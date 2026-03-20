package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxApprovalBridgeServiceImplTest {

    @Test
    void shouldBuildApprovalRequestFromExactOpenfangDetail() {
        AtomicReference<String> capturedTaskRunId = new AtomicReference<>();
        AtomicReference<String> capturedApprovalId = new AtomicReference<>();
        AgentxApprovalBindingMapper bindingMapper = proxy(AgentxApprovalBindingMapper.class, new AtomicReference<>(), "insert");
        AgentxTaskProjectionMapper projectionMapper = proxy(AgentxTaskProjectionMapper.class, new AtomicReference<>(), "updateById");
        OpenfangRuntimeBridge runtimeBridge = (OpenfangRuntimeBridge) Proxy.newProxyInstance(
                OpenfangRuntimeBridge.class.getClassLoader(),
                new Class<?>[] { OpenfangRuntimeBridge.class },
                (proxy, method, args) -> {
                    if ("getApprovalDetail".equals(method.getName())) {
                        capturedTaskRunId.set((String) args[0]);
                        capturedApprovalId.set((String) args[1]);
                        OpenfangApprovalDetailRespDTO detail = new OpenfangApprovalDetailRespDTO();
                        detail.setApprovalId("approval-1");
                        detail.setTaskRunId("task-run-1");
                        detail.setTitle("请假提交审批");
                        detail.setReason("年假 2 天");
                        detail.setRiskLevel(20);
                        detail.setActionSummary("提交请假单");
                        detail.setRequesterId("u-1");
                        return detail;
                    }
                    return null;
                });
        AgentxApprovalBridgeServiceImpl service = createService(bindingMapper, projectionMapper, runtimeBridge);
        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setScenarioCode("oa.leave.approval");
        projection.setBusinessKey("leave:12");
        projection.setOpenfangTaskRunId("task-run-1");

        AgentxApprovalRequest request = service.buildApprovalRequest(projection, "approval-1");

        assertEquals("task-run-1", capturedTaskRunId.get());
        assertEquals("approval-1", capturedApprovalId.get());
        assertEquals("请假提交审批", request.getTitle());
        assertEquals("年假 2 天", request.getReason());
        assertEquals("提交请假单", request.getActionSummary());
        assertEquals("oa.leave.approval", request.getScenarioCode());
        assertEquals("leave:12", request.getBusinessKey());
    }

    @Test
    void shouldCreateApprovalBindingFromOpenfangDetail() {
        AtomicReference<AgentxApprovalBindingDO> inserted = new AtomicReference<>();
        AgentxApprovalBindingMapper bindingMapper = proxy(AgentxApprovalBindingMapper.class, inserted, "insert");
        AgentxTaskProjectionMapper projectionMapper = proxy(AgentxTaskProjectionMapper.class, new AtomicReference<>(), "updateById");
        AgentxApprovalBridgeServiceImpl service = createService(bindingMapper, projectionMapper, null);

        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setId(randomLongId());
        projection.setScenarioCode("oa.leave.approval");
        projection.setBusinessKey("leave:12");
        projection.setOpenfangTaskRunId("task-run-1");
        AgentxApprovalRequest request = new AgentxApprovalRequest()
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:12")
                .setOpenfangTaskRunId("task-run-1")
                .setOpenfangApprovalId("approval-1")
                .setTitle("请假提交审批")
                .setReason("年假 2 天")
                .setRiskLevel(20)
                .setActionSummary("提交请假单")
                .setRequesterId("u-1");

        AgentxApprovalBindingDO result = service.createApprovalBinding(projection, request, "bpm-1");

        assertEquals("oa.leave.approval", result.getScenarioCode());
        assertEquals("leave:12", result.getBusinessKey());
        assertEquals("task-run-1", result.getOpenfangTaskRunId());
        assertEquals("approval-1", result.getOpenfangApprovalId());
        assertEquals("bpm-1", result.getBpmProcessInstanceId());
        assertEquals(Integer.valueOf(20), result.getRiskLevel());
        AssertUtils.assertPojoEquals(result, inserted.get());
    }

    @Test
    void shouldBuildBpmPayloadFromApprovalRequest() {
        AgentxApprovalBindingMapper bindingMapper = proxy(AgentxApprovalBindingMapper.class, new AtomicReference<>(), "insert");
        AgentxTaskProjectionMapper projectionMapper = proxy(AgentxTaskProjectionMapper.class, new AtomicReference<>(), "updateById");
        AgentxApprovalBridgeServiceImpl service = createService(bindingMapper, projectionMapper, null);
        AgentxApprovalRequest request = new AgentxApprovalRequest()
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:12")
                .setOpenfangTaskRunId("task-run-1")
                .setOpenfangApprovalId("approval-1")
                .setTitle("请假提交审批")
                .setReason("年假 2 天")
                .setRiskLevel(20)
                .setActionSummary("提交请假单")
                .setRequesterId("u-1")
                .setApproverSource("dept-manager")
                .setApproverRef("dept:tech");

        AgentxBpmApprovalCreateReq bpmReq = service.buildBpmCreateRequest(request);

        assertEquals("请假提交审批", bpmReq.getTitle());
        assertEquals("提交请假单 | 年假 2 天", bpmReq.getSummary());
        assertEquals("dept-manager", bpmReq.getApproverSource());
        assertEquals("dept:tech", bpmReq.getApproverRef());
    }

    @Test
    void shouldCreateBpmProcessInstanceFromApprovalRequest() {
        AtomicReference<Long> capturedUserId = new AtomicReference<>();
        AtomicReference<BpmProcessInstanceCreateReqDTO> capturedReq = new AtomicReference<>();
        AgentxApprovalBridgeServiceImpl service = createService(
                proxy(AgentxApprovalBindingMapper.class, new AtomicReference<>(), "insert"),
                proxy(AgentxTaskProjectionMapper.class, new AtomicReference<>(), "updateById"),
                null,
                (BpmProcessInstanceService) Proxy.newProxyInstance(
                        BpmProcessInstanceService.class.getClassLoader(),
                        new Class<?>[] { BpmProcessInstanceService.class },
                        (proxy, method, args) -> {
                            if ("createProcessInstance".equals(method.getName())) {
                                capturedUserId.set((Long) args[0]);
                                capturedReq.set((BpmProcessInstanceCreateReqDTO) args[1]);
                                return "bpm-101";
                            }
                            if (method.getReturnType().equals(boolean.class)) {
                                return false;
                            }
                            if (method.getReturnType().equals(int.class) || method.getReturnType().equals(long.class)) {
                                return 0;
                            }
                            return null;
                        }));

        AgentxApprovalRequest request = new AgentxApprovalRequest()
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:12")
                .setOpenfangTaskRunId("task-run-1")
                .setOpenfangApprovalId("approval-1")
                .setTitle("请假提交审批")
                .setReason("年假 2 天")
                .setRiskLevel(20)
                .setActionSummary("提交请假单")
                .setRequesterId("u-1")
                .setApproverSource("dept-manager")
                .setApproverRef("dept:tech");

        String bpmInstanceId = service.createBpmProcessInstance(request);

        assertEquals("bpm-101", bpmInstanceId);
        assertEquals(Long.valueOf(1L), capturedUserId.get());
        assertEquals("oa_leave", capturedReq.get().getProcessDefinitionKey());
        assertEquals("leave:12", capturedReq.get().getBusinessKey());
        Map<String, Object> variables = capturedReq.get().getVariables();
        assertEquals("请假提交审批", variables.get("title"));
        assertEquals("提交请假单 | 年假 2 天", variables.get("summary"));
        assertEquals("approval-1", variables.get("approvalId"));
        assertEquals("task-run-1", variables.get("taskRunId"));
    }

    @Test
    void shouldSyncApprovalDecisionToBindingAndProjection() {
        AtomicReference<AgentxApprovalBindingDO> updatedBinding = new AtomicReference<>();
        AtomicReference<AgentxTaskProjectionDO> updatedProjection = new AtomicReference<>();
        AgentxApprovalBindingMapper bindingMapper = proxy(AgentxApprovalBindingMapper.class, updatedBinding, "updateById");
        AgentxTaskProjectionMapper projectionMapper = proxy(AgentxTaskProjectionMapper.class, updatedProjection, "updateById");
        AgentxApprovalBridgeServiceImpl service = createService(bindingMapper, projectionMapper, null);

        service.syncDecision(12L, 88L, AgentxApprovalBridgeService.ApprovalDecision.APPROVED);

        assertEquals(Long.valueOf(12L), updatedBinding.get().getId());
        assertEquals(Integer.valueOf(10), updatedBinding.get().getDecisionStatus());
        assertEquals(Long.valueOf(88L), updatedProjection.get().getId());
        assertEquals(AgentxTaskProjectionStatusEnum.APPROVED.getStatus(), updatedProjection.get().getProjectionStatus());
    }

    @Test
    void shouldResolveTimeoutOutcomeToCompensatedProjection() {
        AtomicReference<AgentxApprovalBindingDO> updatedBinding = new AtomicReference<>();
        AtomicReference<AgentxTaskProjectionDO> updatedProjection = new AtomicReference<>();
        AgentxApprovalBindingMapper bindingMapper = proxy(AgentxApprovalBindingMapper.class, updatedBinding, "updateById");
        AgentxTaskProjectionMapper projectionMapper = proxy(AgentxTaskProjectionMapper.class, updatedProjection, "updateById");
        AgentxApprovalBridgeServiceImpl service = createService(bindingMapper, projectionMapper, null);

        AgentxApprovalResolution resolution = service.resolveOutcome(12L, 88L, AgentxApprovalOutcome.TIMEOUT);

        assertEquals(Long.valueOf(12L), updatedBinding.get().getId());
        assertEquals(Integer.valueOf(40), updatedBinding.get().getDecisionStatus());
        assertEquals(Long.valueOf(88L), updatedProjection.get().getId());
        assertEquals(AgentxTaskProjectionStatusEnum.COMPENSATED.getStatus(), updatedProjection.get().getProjectionStatus());
        assertEquals(AgentxApprovalRuntimeAction.REJECT, resolution.getRuntimeAction());
    }

    private AgentxApprovalBridgeServiceImpl createService(AgentxApprovalBindingMapper bindingMapper,
                                                          AgentxTaskProjectionMapper projectionMapper,
                                                          OpenfangRuntimeBridge runtimeBridge) {
        return createService(bindingMapper, projectionMapper, runtimeBridge, null);
    }

    private AgentxApprovalBridgeServiceImpl createService(AgentxApprovalBindingMapper bindingMapper,
                                                          AgentxTaskProjectionMapper projectionMapper,
                                                          OpenfangRuntimeBridge runtimeBridge,
                                                          BpmProcessInstanceService processInstanceService) {
        AgentxApprovalBridgeServiceImpl service = new AgentxApprovalBridgeServiceImpl();
        ReflectionTestUtils.setField(service, "approvalBindingMapper", bindingMapper);
        ReflectionTestUtils.setField(service, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(service, "runtimeBridge", runtimeBridge);
        ReflectionTestUtils.setField(service, "bpmProcessInstanceService", processInstanceService);
        return service;
    }

    @SuppressWarnings("unchecked")
    private <T, V> T proxy(Class<T> type, AtomicReference<V> sink, String capturedMethod) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
            if (method.getName().equals(capturedMethod)) {
                sink.set((V) args[0]);
                return 1;
            }
            if (method.getReturnType().equals(boolean.class)) {
                return false;
            }
            if (method.getReturnType().equals(int.class) || method.getReturnType().equals(long.class)) {
                return 0;
            }
            return null;
        });
    }

}
