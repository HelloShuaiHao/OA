package cn.iocoder.yudao.module.agentx.service.task;

import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                    return null;
                });
    }

}
