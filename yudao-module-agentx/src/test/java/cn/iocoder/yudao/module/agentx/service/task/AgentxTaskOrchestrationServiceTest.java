package cn.iocoder.yudao.module.agentx.service.task;

import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowMapping;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowResolver;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxTaskOrchestrationServiceTest {

    @Test
    void shouldResolveWorkflowAndPersistTaskProjection() {
        AtomicReference<AgentxTaskProjectionDO> inserted = new AtomicReference<>();
        AtomicReference<OpenfangWorkflowRunReqDTO> runtimeRequest = new AtomicReference<>();
        AgentxTaskProjectionMapper taskProjectionMapper = proxyTaskProjectionMapper(inserted);
        OpenfangRuntimeBridge runtimeBridge = proxyRuntimeBridge(runtimeRequest);
        AgentxTaskOrchestrationService service = createService(taskProjectionMapper, runtimeBridge);

        AgentxTaskStartRequest request = new AgentxTaskStartRequest()
                .setScenarioCode("oa.leave.approval")
                .setWorkflowVersion("v1")
                .setBusinessKey("leave:1")
                .setIdempotencyKey("idem-1")
                .setPrincipalType("human")
                .setPrincipalId("u-1")
                .setCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE))
                .setContextBundle(Collections.singletonMap("leave.form", "L-1"));

        AgentxTaskProjectionDO projection = service.startTask(request, Collections.singletonList(
                new AgentxWorkflowMapping("oa.leave.approval", "openfang-leave", "v1", true)
        ));

        assertEquals("openfang-leave", runtimeRequest.get().getScenarioCode());
        assertEquals("task-run-1", projection.getOpenfangTaskRunId());
        assertEquals("leave:1", inserted.get().getBusinessKey());
        assertEquals("oa.leave.approval", inserted.get().getScenarioCode());
    }

    @Test
    void shouldRefreshProjectionFromRuntimeTask() {
        AtomicReference<AgentxTaskProjectionDO> updated = new AtomicReference<>();
        AgentxTaskProjectionMapper taskProjectionMapper = (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("updateById".equals(method.getName())) {
                        updated.set((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    return null;
                });
        AgentxTaskOrchestrationService service = createService(taskProjectionMapper, proxyRuntimeBridge(new AtomicReference<>()));
        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setId(9L);
        OpenfangTaskRespDTO taskRespDTO = new OpenfangTaskRespDTO();
        taskRespDTO.setStatus("WAITING_APPROVAL");
        taskRespDTO.setResultSummary("waiting");
        taskRespDTO.setFailureSummary(null);
        taskRespDTO.setAuditSummary("audit");

        service.refreshProjection(projection, taskRespDTO);

        assertEquals(Integer.valueOf(20), updated.get().getProjectionStatus());
        assertEquals("waiting", updated.get().getResultSummary());
        assertEquals("audit", updated.get().getAuditSummary());
    }

    @Test
    void shouldReturnExistingProjectionWhenIdempotencyKeyDuplicated() {
        AgentxTaskProjectionDO existing = new AgentxTaskProjectionDO();
        existing.setId(100L);
        existing.setIdempotencyKey("idem-dup");
        existing.setOpenfangTaskRunId("task-run-exists");
        AtomicReference<OpenfangWorkflowRunReqDTO> runtimeRequest = new AtomicReference<>();
        AgentxTaskProjectionMapper mapper = proxyProjectionMapperWithExisting(existing);
        OpenfangRuntimeBridge runtimeBridge = proxyRuntimeBridge(runtimeRequest);
        AgentxTaskOrchestrationService service = createService(mapper, runtimeBridge);

        AgentxTaskProjectionDO projection = service.createTask(new AgentxTaskStartRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setWorkflowVersion("v1")
                        .setBusinessKey("leave:dup")
                        .setIdempotencyKey("idem-dup")
                        .setPrincipalType("human")
                        .setPrincipalId("u-1")
                        .setCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE))
                        .setContextBundle(Collections.singletonMap("leave.form", "L-dup")),
                Collections.singletonList(new AgentxWorkflowMapping("oa.leave.approval", "openfang-leave", "v1", true)));

        assertEquals(Long.valueOf(100L), projection.getId());
        assertEquals("task-run-exists", projection.getOpenfangTaskRunId());
    }

    @Test
    void shouldCalculateBackoffAndTimeout() {
        AgentxTaskProjectionDO updateSink = new AgentxTaskProjectionDO();
        AgentxTaskProjectionMapper mapper = (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("updateById".equals(method.getName())) {
                        AgentxTaskProjectionDO update = (AgentxTaskProjectionDO) args[0];
                        updateSink.setProjectionStatus(update.getProjectionStatus());
                        updateSink.setFailureSummary(update.getFailureSummary());
                        return 1;
                    }
                    return null;
                });
        AgentxTaskOrchestrationService service = createService(mapper, proxyRuntimeBridge(new AtomicReference<>()));
        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setId(9L);
        projection.setProjectionStatus(AgentxTaskProjectionStatusEnum.RUNNING.getStatus());
        projection.setUpdateTime(LocalDateTime.now().minusHours(25));

        assertEquals(Integer.valueOf(2), service.nextPollIntervalSeconds(1));
        assertEquals(Integer.valueOf(4), service.nextPollIntervalSeconds(2));
        assertEquals(Integer.valueOf(30), service.nextPollIntervalSeconds(8));
        assertTrue(service.markFailedIfTimedOut(projection, LocalDateTime.now()));
        assertEquals(AgentxTaskProjectionStatusEnum.FAILED.getStatus(), updateSink.getProjectionStatus());
        assertNotNull(updateSink.getFailureSummary());
    }

    @Test
    void shouldNotMarkTimeoutForTerminalStatus() {
        AgentxTaskOrchestrationService service = createService(proxyTaskProjectionMapper(new AtomicReference<>()),
                proxyRuntimeBridge(new AtomicReference<>()));
        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setId(10L);
        projection.setProjectionStatus(AgentxTaskProjectionStatusEnum.SUCCEEDED.getStatus());
        projection.setUpdateTime(LocalDateTime.now().minusHours(30));

        assertFalse(service.markFailedIfTimedOut(projection, LocalDateTime.now()));
    }

    @Test
    void shouldGetTaskRunFromRuntimeBridge() {
        AgentxTaskOrchestrationService service = createService(proxyTaskProjectionMapper(new AtomicReference<>()),
                proxyRuntimeBridge(new AtomicReference<>()));

        OpenfangTaskRespDTO task = service.getTaskRun("tr-123");

        assertEquals("tr-123", task.getTaskRunId());
        assertEquals("running", task.getStatus());
    }

    @Test
    void shouldCreateTaskThenPollRuntimeAndRefreshProjection() {
        AtomicReference<AgentxTaskProjectionDO> inserted = new AtomicReference<>();
        AtomicReference<AgentxTaskProjectionDO> updated = new AtomicReference<>();
        AgentxTaskProjectionMapper mapper = (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        inserted.set((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        updated.set((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    return null;
                });
        OpenfangRuntimeBridge runtimeBridge = (OpenfangRuntimeBridge) Proxy.newProxyInstance(
                OpenfangRuntimeBridge.class.getClassLoader(),
                new Class<?>[] { OpenfangRuntimeBridge.class },
                (proxy, method, args) -> {
                    if ("runWorkflow".equals(method.getName())) {
                        OpenfangWorkflowRunRespDTO resp = new OpenfangWorkflowRunRespDTO();
                        resp.setTaskRunId("task-run-69");
                        resp.setTaskStatus("RUNNING");
                        return resp;
                    }
                    if ("getTaskRun".equals(method.getName()) || "getTask".equals(method.getName())) {
                        OpenfangTaskRespDTO resp = new OpenfangTaskRespDTO();
                        resp.setTaskRunId((String) args[0]);
                        resp.setStatus("WAITING_APPROVAL");
                        resp.setResultSummary("waiting");
                        resp.setAuditSummary("poll-audit");
                        return resp;
                    }
                    return null;
                });
        AgentxTaskOrchestrationService service = createService(mapper, runtimeBridge);

        AgentxTaskProjectionDO projection = service.startTask(new AgentxTaskStartRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setWorkflowVersion("v1")
                        .setBusinessKey("leave:69")
                        .setIdempotencyKey("idem-69")
                        .setPrincipalType("human")
                        .setPrincipalId("u-69")
                        .setCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE))
                        .setContextBundle(Collections.singletonMap("leave.form", "L-69")),
                Collections.singletonList(new AgentxWorkflowMapping("oa.leave.approval", "openfang-leave", "v1", true)));
        OpenfangTaskRespDTO taskRun = service.getTaskRun("task-run-69");
        service.refreshProjection(projection, taskRun);

        assertEquals("leave:69", inserted.get().getBusinessKey());
        assertEquals("task-run-69", inserted.get().getOpenfangTaskRunId());
        assertEquals(Integer.valueOf(20), updated.get().getProjectionStatus());
        assertEquals("waiting", updated.get().getResultSummary());
    }

    private AgentxTaskOrchestrationService createService(AgentxTaskProjectionMapper mapper,
                                                         OpenfangRuntimeBridge runtimeBridge) {
        AgentxTaskOrchestrationService service = new AgentxTaskOrchestrationService(new AgentxWorkflowResolver());
        ReflectionTestUtils.setField(service, "taskProjectionMapper", mapper);
        ReflectionTestUtils.setField(service, "runtimeBridge", runtimeBridge);
        return service;
    }

    @SuppressWarnings("unchecked")
    private AgentxTaskProjectionMapper proxyTaskProjectionMapper(AtomicReference<AgentxTaskProjectionDO> sink) {
        return (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        sink.set((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        sink.set((AgentxTaskProjectionDO) args[0]);
                        return 1;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private OpenfangRuntimeBridge proxyRuntimeBridge(AtomicReference<OpenfangWorkflowRunReqDTO> requestSink) {
        return (OpenfangRuntimeBridge) Proxy.newProxyInstance(
                OpenfangRuntimeBridge.class.getClassLoader(),
                new Class<?>[] { OpenfangRuntimeBridge.class },
                (proxy, method, args) -> {
                    if ("runWorkflow".equals(method.getName())) {
                        requestSink.set((OpenfangWorkflowRunReqDTO) args[1]);
                        OpenfangWorkflowRunRespDTO response = new OpenfangWorkflowRunRespDTO();
                        response.setTaskRunId("task-run-1");
                        response.setWorkflowId((String) args[0]);
                        response.setWorkflowVersion("v1");
                        response.setTaskStatus("RUNNING");
                        return response;
                    }
                    if ("getTaskRun".equals(method.getName()) || "getTask".equals(method.getName())) {
                        OpenfangTaskRespDTO response = new OpenfangTaskRespDTO();
                        response.setTaskRunId((String) args[0]);
                        response.setStatus("running");
                        return response;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private AgentxTaskProjectionMapper proxyProjectionMapperWithExisting(AgentxTaskProjectionDO existing) {
        return (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("selectByIdempotencyKey".equals(method.getName())) {
                        return existing;
                    }
                    return null;
                });
    }

}
