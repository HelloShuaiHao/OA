package cn.iocoder.yudao.module.agentx.service.context;

import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.scenario.AgentxScenarioConfigMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxContextAssemblyServiceTest {

    @Test
    void shouldAssembleContextByScenarioConfigProviders() {
        AgentxContextAssemblyService service = new AgentxContextAssemblyService();
        ReflectionTestUtils.setField(service, "providers", Arrays.asList(
                new ContextProvider() {
                    @Override
                    public String getType() {
                        return "bpm_tasks";
                    }

                    @Override
                    public Map<String, Object> provide(ContextRequest request) {
                        return Collections.singletonMap("tasks", Collections.singletonList("T-1"));
                    }
                },
                new ContextProvider() {
                    @Override
                    public String getType() {
                        return "user_profile";
                    }

                    @Override
                    public Map<String, Object> provide(ContextRequest request) {
                        return Collections.singletonMap("name", "张三");
                    }
                }
        ));
        ReflectionTestUtils.setField(service, "scenarioConfigMapper", proxyScenarioMapper(
                "{\"contextProviders\":[{\"type\":\"bpm_tasks\",\"filter\":{\"processKey\":\"leave\",\"status\":\"pending\"}},"
                        + "{\"type\":\"user_profile\",\"fields\":[\"name\"]}]}"
        ));

        AgentxContextRequest request = new AgentxContextRequest("oa.leave.approval", "leave:1", new HashMap<>())
                .setUserId(100L);
        BusinessContextBundle bundle = service.assemble(request);

        assertEquals("oa.leave.approval", bundle.getScenarioCode());
        assertNotNull(bundle.getContext().get("bpm_tasks"));
        assertNotNull(bundle.getContext().get("user_profile"));
        assertTrue(bundle.getSummaryContext().containsKey("contextSummary"));
    }

    @Test
    void shouldKeepSeedWhenNoScenarioConfig() {
        AgentxContextAssemblyService service = new AgentxContextAssemblyService();
        ReflectionTestUtils.setField(service, "providers", Collections.emptyList());
        ReflectionTestUtils.setField(service, "scenarioConfigMapper", proxyScenarioMapper(null));

        Map<String, Object> seed = new HashMap<>();
        seed.put("leave.form", "L-1");
        BusinessContextBundle bundle = service.assemble(new AgentxContextRequest("oa.leave.approval", "leave:1", seed));

        assertEquals("L-1", bundle.getContext().get("leave.form"));
        assertNotNull(bundle.getSnapshot());
    }

    private AgentxScenarioConfigMapper proxyScenarioMapper(String configJson) {
        return (AgentxScenarioConfigMapper) Proxy.newProxyInstance(
                AgentxScenarioConfigMapper.class.getClassLoader(),
                new Class<?>[] { AgentxScenarioConfigMapper.class },
                (proxy, method, args) -> {
                    if ("selectByScenarioCode".equals(method.getName())) {
                        if (configJson == null) {
                            return null;
                        }
                        return new AgentxScenarioConfigDO().setScenarioCode((String) args[0]).setConfig(configJson);
                    }
                    return null;
                });
    }

}
