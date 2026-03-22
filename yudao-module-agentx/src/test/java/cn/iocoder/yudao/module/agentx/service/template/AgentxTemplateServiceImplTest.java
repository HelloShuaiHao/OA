package cn.iocoder.yudao.module.agentx.service.template;

import cn.iocoder.yudao.module.agentx.controller.admin.template.vo.AgentxTemplateRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.template.AgentxAgentTemplateDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.template.AgentxAgentTemplateMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AgentxTemplateServiceImplTest {

    private final AgentxAgentTemplateMapper mapper = Mockito.mock(AgentxAgentTemplateMapper.class);
    private final AgentxTemplateServiceImpl service = buildService();

    @Test
    void shouldParseTemplateJsonFields() {
        AgentxAgentTemplateDO template = new AgentxAgentTemplateDO()
                .setTemplateType("leave")
                .setTemplateName("请假审批助手")
                .setDescription("自动处理请假")
                .setDefaultCapabilities("[\"approve_task\",\"query_task\"]")
                .setRecommendedProcessKeys("[\"leave_standard\"]")
                .setDefaultRules("[{\"field\":\"leaveDays\",\"operator\":\"<=\",\"value\":\"2\",\"processDefinitionKey\":\"leave_standard\"}]")
                .setEnabled(true);
        when(mapper.selectEnabledList()).thenReturn(Collections.singletonList(template));

        List<AgentxTemplateRespVO> list = service.getTemplateList();
        assertEquals(1, list.size());
        assertEquals("leave", list.get(0).getTemplateType());
        assertEquals(2, list.get(0).getDefaultCapabilities().size());
        assertEquals("leave_standard", list.get(0).getRecommendedProcessKeys().get(0));
        assertEquals("leaveDays", list.get(0).getDefaultRules().get(0).getField());
    }

    private AgentxTemplateServiceImpl buildService() {
        AgentxTemplateServiceImpl impl = new AgentxTemplateServiceImpl();
        try {
            java.lang.reflect.Field field = AgentxTemplateServiceImpl.class.getDeclaredField("templateMapper");
            field.setAccessible(true);
            field.set(impl, mapper);
            return impl;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
