package cn.iocoder.yudao.module.agentx.service.process;

import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentProcessDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentProcessMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AgentxProcessSelectionServiceImplTest {

    private final AgentxAgentProcessMapper processMapper = Mockito.mock(AgentxAgentProcessMapper.class);
    private final AgentxProcessSelectionServiceImpl service = buildService();

    @Test
    void shouldSelectProcessByRule() {
        AgentxAgentProcessDO standard = new AgentxAgentProcessDO()
                .setAgentId(1L).setProcessDefinitionId("pd-standard")
                .setProcessDefinitionKey("leave_approval_standard").setProcessName("标准请假流程")
                .setPriority(20).setSelectionMode("rule")
                .setSelectionRules("[{\"field\":\"leaveDays\",\"operator\":\"<=\",\"value\":\"2\",\"processDefinitionKey\":\"leave_approval_standard\"}]");
        AgentxAgentProcessDO manager = new AgentxAgentProcessDO()
                .setAgentId(1L).setProcessDefinitionId("pd-manager")
                .setProcessDefinitionKey("leave_approval_manager").setProcessName("主管请假流程")
                .setPriority(10).setSelectionMode("rule");
        when(processMapper.selectListByAgentId(1L)).thenReturn(Arrays.asList(standard, manager));

        AgentxProcessSelectionService.SelectionResult result = service.selectProcess(1L, "rule", Map.of("leaveDays", 2));
        assertEquals("pd-standard", result.getProcessDefinitionId());
    }

    @Test
    void shouldFallbackToHighestPriorityWhenNoRuleMatched() {
        AgentxAgentProcessDO first = new AgentxAgentProcessDO()
                .setAgentId(1L).setProcessDefinitionId("pd-first")
                .setProcessDefinitionKey("p1").setProcessName("流程1")
                .setPriority(100).setSelectionMode("rule")
                .setSelectionRules("[{\"field\":\"amount\",\"operator\":\">\",\"value\":\"1000\",\"processDefinitionKey\":\"p2\"}]");
        AgentxAgentProcessDO second = new AgentxAgentProcessDO()
                .setAgentId(1L).setProcessDefinitionId("pd-second")
                .setProcessDefinitionKey("p2").setProcessName("流程2")
                .setPriority(10).setSelectionMode("rule");
        when(processMapper.selectListByAgentId(1L)).thenReturn(Arrays.asList(first, second));

        AgentxProcessSelectionService.SelectionResult result = service.selectProcess(1L, "rule", Map.of("amount", 100));
        assertEquals("pd-first", result.getProcessDefinitionId());
    }

    @Test
    void shouldSelectOnlyProcessDirectly() {
        AgentxAgentProcessDO single = new AgentxAgentProcessDO()
                .setAgentId(1L).setProcessDefinitionId("pd-1")
                .setProcessDefinitionKey("p1").setProcessName("流程1")
                .setSelectionMode("auto");
        when(processMapper.selectListByAgentId(1L)).thenReturn(Collections.singletonList(single));

        AgentxProcessSelectionService.SelectionResult result = service.selectProcess(1L, "auto", Map.of());
        assertEquals("pd-1", result.getProcessDefinitionId());
    }

    private AgentxProcessSelectionServiceImpl buildService() {
        AgentxProcessSelectionServiceImpl impl = new AgentxProcessSelectionServiceImpl();
        try {
            java.lang.reflect.Field field = AgentxProcessSelectionServiceImpl.class.getDeclaredField("processMapper");
            field.setAccessible(true);
            field.set(impl, processMapper);
            return impl;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
