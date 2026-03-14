package cn.iocoder.yudao.module.agentx.service.task;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeServiceImpl;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalOutcome;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRequest;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowMapping;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowResolver;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxTaskLifecycleServiceTest {

    @Test
    void shouldRunLeaveTaskThenBuildPendingApprovalAndPushDecision() {
        AtomicReference<AgentxTaskProjectionDO> insertedProjection = new AtomicReference<>();
        List<AgentxTaskProjectionDO> updatedProjections = new CopyOnWriteArrayList<>();
        AtomicReference<AgentxApprovalBindingDO> insertedBinding = new AtomicReference<>();
        AtomicReference<AgentxApprovalBindingDO> updatedBinding = new AtomicReference<>();
        AtomicReference<String> approvedId = new AtomicReference<>();
        AtomicReference<String> approvedComment = new AtomicReference<>();
        List<String> auditEvents = new CopyOnWriteArrayList<>();
        OpenfangRuntimeBridge runtimeBridge = proxyRuntimeBridge(approvedId, approvedComment);
        AgentxTaskProjectionMapper projectionMapper = proxyProjectionMapper(insertedProjection, updatedProjections);
        AgentxApprovalBindingMapper bindingMapper = proxyBindingMapper(insertedBinding, updatedBinding);
        AgentxAuditService auditService = proxyAuditService(auditEvents);

        AgentxTaskOrchestrationService orchestrationService = new AgentxTaskOrchestrationService(new AgentxWorkflowResolver());
        ReflectionTestUtils.setField(orchestrationService, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(orchestrationService, "runtimeBridge", runtimeBridge);

        AgentxApprovalBridgeServiceImpl approvalBridgeService = new AgentxApprovalBridgeServiceImpl();
        ReflectionTestUtils.setField(approvalBridgeService, "approvalBindingMapper", bindingMapper);
        ReflectionTestUtils.setField(approvalBridgeService, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(approvalBridgeService, "runtimeBridge", runtimeBridge);

        AgentxTaskLifecycleService lifecycleService = new AgentxTaskLifecycleService(
                orchestrationService, approvalBridgeService, runtimeBridge, auditService);

        AgentxTaskProjectionDO projection = lifecycleService.startTask(new AgentxTaskStartRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setWorkflowVersion("v1")
                        .setBusinessKey("leave:99")
                        .setIdempotencyKey("idem-99")
                        .setPrincipalType("human")
                        .setPrincipalId("u-1")
                        .setCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE))
                        .setContextBundle(Collections.singletonMap("leave.form", "L-99")),
                Collections.singletonList(new AgentxWorkflowMapping("oa.leave.approval", "leave-workflow", "v1", true)));

        AgentxApprovalRequest approvalRequest = lifecycleService.pullPendingApprovalRequest(projection);
        AgentxApprovalBindingDO binding = lifecycleService.createApprovalBinding(projection, approvalRequest, "bpm-99");
        lifecycleService.resolveApproval(binding, 99L, AgentxApprovalBridgeService.ApprovalDecision.APPROVED, "同意");

        assertEquals("task-run-1", projection.getOpenfangTaskRunId());
        assertEquals("leave:99", insertedProjection.get().getBusinessKey());
        assertEquals(AgentxTaskProjectionStatusEnum.WAITING_APPROVAL.getStatus(), updatedProjections.get(0).getProjectionStatus());
        assertEquals(AgentxTaskProjectionStatusEnum.APPROVED.getStatus(), updatedProjections.get(1).getProjectionStatus());
        assertNotNull(approvalRequest);
        assertEquals("请假提交审批", approvalRequest.getTitle());
        assertEquals("年假 2 天", approvalRequest.getReason());
        assertEquals("approval-1", insertedBinding.get().getOpenfangApprovalId());
        assertEquals("bpm-99", insertedBinding.get().getBpmProcessInstanceId());
        assertEquals(Long.valueOf(99L), updatedBinding.get().getId());
        assertEquals("approval-1", approvedId.get());
        assertEquals("同意", approvedComment.get());
        assertEquals(List.of("TASK_STARTED", "TASK_WAITING_APPROVAL", "APPROVAL_APPROVED"), auditEvents);
    }

    @Test
    void shouldAuditAndRejectStartWhenDelegationExpired() {
        AtomicReference<AgentxTaskProjectionDO> insertedProjection = new AtomicReference<>();
        List<AgentxTaskProjectionDO> updatedProjections = new CopyOnWriteArrayList<>();
        List<String> auditEvents = new CopyOnWriteArrayList<>();
        OpenfangRuntimeBridge runtimeBridge = proxyRuntimeBridge(new AtomicReference<>(), new AtomicReference<>());
        AgentxTaskProjectionMapper projectionMapper = proxyProjectionMapper(insertedProjection, updatedProjections);
        AgentxApprovalBindingMapper bindingMapper = proxyBindingMapper(new AtomicReference<>(), new AtomicReference<>());
        AgentxAuditService auditService = proxyAuditService(auditEvents);

        AgentxTaskOrchestrationService orchestrationService = new AgentxTaskOrchestrationService(new AgentxWorkflowResolver());
        ReflectionTestUtils.setField(orchestrationService, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(orchestrationService, "runtimeBridge", runtimeBridge);

        AgentxApprovalBridgeServiceImpl approvalBridgeService = new AgentxApprovalBridgeServiceImpl();
        ReflectionTestUtils.setField(approvalBridgeService, "approvalBindingMapper", bindingMapper);
        ReflectionTestUtils.setField(approvalBridgeService, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(approvalBridgeService, "runtimeBridge", runtimeBridge);

        AgentxTaskLifecycleService lifecycleService = new AgentxTaskLifecycleService(
                orchestrationService, approvalBridgeService, runtimeBridge, auditService, new AgentxAuthorizationService());

        ServiceException ex = assertThrows(ServiceException.class, () -> lifecycleService.startAuthorizedTask(
                new AgentxTaskStartRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setWorkflowVersion("v1")
                        .setBusinessKey("leave:100")
                        .setIdempotencyKey("idem-100")
                        .setPrincipalType("human")
                        .setPrincipalId("u-1")
                        .setCapabilities(EnumSet.of(AgentxCapability.SUBMIT_LEAVE))
                        .setContextBundle(Collections.singletonMap("leave.form", "L-100")),
                Collections.singletonList(new AgentxWorkflowMapping("oa.leave.approval", "leave-workflow", "v1", true)),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE),
                new ExecutionIdentity().setDelegationActive(false)
                        .setDelegatedCapabilities(EnumSet.of(AgentxCapability.SUBMIT_LEAVE)),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE)
        ));

        assertEquals(Integer.valueOf(1_024_001_005), ex.getCode());
        assertTrue(updatedProjections.isEmpty());
        assertTrue(auditEvents.contains("AUTHORIZATION_DENIED"));
    }

    @Test
    void shouldRejectAndCompensateWhenApprovalTimeout() {
        AtomicReference<AgentxTaskProjectionDO> insertedProjection = new AtomicReference<>();
        List<AgentxTaskProjectionDO> updatedProjections = new CopyOnWriteArrayList<>();
        AtomicReference<AgentxApprovalBindingDO> insertedBinding = new AtomicReference<>();
        AtomicReference<AgentxApprovalBindingDO> updatedBinding = new AtomicReference<>();
        AtomicReference<String> approvedId = new AtomicReference<>();
        AtomicReference<String> approvedComment = new AtomicReference<>();
        List<String> auditEvents = new CopyOnWriteArrayList<>();
        OpenfangRuntimeBridge runtimeBridge = proxyRuntimeBridge(approvedId, approvedComment);
        AgentxTaskProjectionMapper projectionMapper = proxyProjectionMapper(insertedProjection, updatedProjections);
        AgentxApprovalBindingMapper bindingMapper = proxyBindingMapper(insertedBinding, updatedBinding);
        AgentxAuditService auditService = proxyAuditService(auditEvents);

        AgentxTaskOrchestrationService orchestrationService = new AgentxTaskOrchestrationService(new AgentxWorkflowResolver());
        ReflectionTestUtils.setField(orchestrationService, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(orchestrationService, "runtimeBridge", runtimeBridge);

        AgentxApprovalBridgeServiceImpl approvalBridgeService = new AgentxApprovalBridgeServiceImpl();
        ReflectionTestUtils.setField(approvalBridgeService, "approvalBindingMapper", bindingMapper);
        ReflectionTestUtils.setField(approvalBridgeService, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(approvalBridgeService, "runtimeBridge", runtimeBridge);

        AgentxTaskLifecycleService lifecycleService = new AgentxTaskLifecycleService(
                orchestrationService, approvalBridgeService, runtimeBridge, auditService);
        AgentxApprovalBindingDO binding = new AgentxApprovalBindingDO();
        binding.setId(99L);
        binding.setOpenfangApprovalId("approval-1");

        AgentxApprovalCallbackResult result = lifecycleService.resolveApprovalOutcome(binding, 99L,
                AgentxApprovalOutcome.TIMEOUT, "审批超时");

        assertEquals("approval-1", approvedId.get());
        assertEquals("审批超时", approvedComment.get());
        assertEquals(Integer.valueOf(40), updatedBinding.get().getDecisionStatus());
        assertEquals(AgentxTaskProjectionStatusEnum.COMPENSATED.getStatus(), updatedProjections.get(0).getProjectionStatus());
        assertTrue(result.isProcessed());
        assertTrue(result.isCompensationRequired());
    }

    @Test
    void shouldIgnoreDuplicateApprovalCallbackAfterDecisionPersisted() {
        OpenfangRuntimeBridge runtimeBridge = proxyRuntimeBridge(new AtomicReference<>(), new AtomicReference<>());
        AgentxTaskOrchestrationService orchestrationService = new AgentxTaskOrchestrationService(new AgentxWorkflowResolver());
        AgentxApprovalBridgeServiceImpl approvalBridgeService = new AgentxApprovalBridgeServiceImpl();
        ReflectionTestUtils.setField(approvalBridgeService, "approvalBindingMapper", proxyBindingMapper(new AtomicReference<>(), new AtomicReference<>()));
        ReflectionTestUtils.setField(approvalBridgeService, "taskProjectionMapper", proxyProjectionMapper(new AtomicReference<>(), new CopyOnWriteArrayList<>()));
        ReflectionTestUtils.setField(approvalBridgeService, "runtimeBridge", runtimeBridge);
        AgentxTaskLifecycleService lifecycleService = new AgentxTaskLifecycleService(orchestrationService, approvalBridgeService, runtimeBridge, null);
        AgentxApprovalBindingDO binding = new AgentxApprovalBindingDO();
        binding.setId(99L);
        binding.setOpenfangApprovalId("approval-1");
        binding.setDecisionStatus(10);

        AgentxApprovalCallbackResult result = lifecycleService.resolveApprovalOutcome(binding, 99L,
                AgentxApprovalOutcome.APPROVED, "重复回调");

        assertTrue(result.isIgnored());
        assertEquals("DUPLICATE_CALLBACK", result.getReason());
    }

    @SuppressWarnings("unchecked")
    private OpenfangRuntimeBridge proxyRuntimeBridge(AtomicReference<String> approvedId,
                                                     AtomicReference<String> approvedComment) {
        return (OpenfangRuntimeBridge) Proxy.newProxyInstance(
                OpenfangRuntimeBridge.class.getClassLoader(),
                new Class<?>[] { OpenfangRuntimeBridge.class },
                (proxy, method, args) -> {
                    if ("runWorkflow".equals(method.getName())) {
                        OpenfangWorkflowRunRespDTO response = new OpenfangWorkflowRunRespDTO();
                        response.setTaskRunId("task-run-1");
                        response.setWorkflowId((String) args[0]);
                        response.setWorkflowVersion("v1");
                        response.setTaskStatus("RUNNING");
                        return response;
                    }
                    if ("getTask".equals(method.getName())) {
                        OpenfangTaskRespDTO task = new OpenfangTaskRespDTO();
                        task.setTaskRunId((String) args[0]);
                        task.setStatus("WAITING_APPROVAL");
                        task.setPendingApprovalIds(List.of("approval-1"));
                        task.setResultSummary("waiting");
                        task.setAuditSummary("audit");
                        return task;
                    }
                    if ("getApprovalDetail".equals(method.getName())) {
                        OpenfangApprovalDetailRespDTO detail = new OpenfangApprovalDetailRespDTO();
                        detail.setApprovalId((String) args[1]);
                        detail.setTaskRunId((String) args[0]);
                        detail.setTitle("请假提交审批");
                        detail.setReason("年假 2 天");
                        detail.setRiskLevel(20);
                        detail.setActionSummary("提交请假单");
                        detail.setRequesterId("u-1");
                        return detail;
                    }
                    if ("approve".equals(method.getName())) {
                        approvedId.set((String) args[0]);
                        approvedComment.set((String) args[1]);
                        return null;
                    }
                    if ("reject".equals(method.getName())) {
                        approvedId.set((String) args[0]);
                        approvedComment.set((String) args[1]);
                        return null;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private AgentxTaskProjectionMapper proxyProjectionMapper(AtomicReference<AgentxTaskProjectionDO> inserted,
                                                             List<AgentxTaskProjectionDO> updated) {
        return (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        inserted.set((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        updated.add((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private AgentxApprovalBindingMapper proxyBindingMapper(AtomicReference<AgentxApprovalBindingDO> inserted,
                                                           AtomicReference<AgentxApprovalBindingDO> updated) {
        return (AgentxApprovalBindingMapper) Proxy.newProxyInstance(
                AgentxApprovalBindingMapper.class.getClassLoader(),
                new Class<?>[] { AgentxApprovalBindingMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        AgentxApprovalBindingDO binding = (AgentxApprovalBindingDO) args[0];
                        if (binding.getId() == null) {
                            binding.setId(99L);
                        }
                        inserted.set(binding);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        updated.set((AgentxApprovalBindingDO) args[0]);
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private AgentxAuditService proxyAuditService(List<String> sink) {
        return (AgentxAuditService) Proxy.newProxyInstance(
                AgentxAuditService.class.getClassLoader(),
                new Class<?>[] { AgentxAuditService.class },
                (proxy, method, args) -> {
                    sink.add(method.getName()
                            .replace("recordTaskStarted", "TASK_STARTED")
                            .replace("recordPendingApproval", "TASK_WAITING_APPROVAL")
                            .replace("recordApprovalDecision", "APPROVAL_APPROVED")
                            .replace("recordAuthorizationDenied", "AUTHORIZATION_DENIED"));
                    return null;
                });
    }

}
