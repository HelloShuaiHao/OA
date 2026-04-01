package cn.iocoder.yudao.module.agentx.service.agent;

import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentCapabilityDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentDO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxAgentServiceImplGuardrailTest {

    @Test
    void shouldInjectIdentityEntitlementGuardrailIntoSystemPrompt() throws Exception {
        AgentxAgentServiceImpl service = new AgentxAgentServiceImpl();
        AgentxAgentDO agent = new AgentxAgentDO()
                .setAgentName("调度助手")
                .setDeptName("物流调度部")
                .setTemplateType("custom")
                .setDescription("负责调度计划建议");
        AgentxAgentCapabilityDO capability = new AgentxAgentCapabilityDO()
                .setCapabilityName("路线查询")
                .setConditions("仅华东区");

        Method method = AgentxAgentServiceImpl.class
                .getDeclaredMethod("buildOpenfangSystemPrompt", AgentxAgentDO.class, List.class);
        method.setAccessible(true);
        String prompt = (String) method.invoke(service, agent, List.of(capability));

        assertTrue(prompt.contains("安全护栏"));
        assertTrue(prompt.contains("只能基于系统提供的当前会话用户上下文回答"));
        assertTrue(prompt.contains("我只能提供您自己的访问信息"));
    }
}
