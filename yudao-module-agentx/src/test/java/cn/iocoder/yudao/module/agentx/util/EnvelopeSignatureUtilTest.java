package cn.iocoder.yudao.module.agentx.util;

import cn.iocoder.yudao.module.agentx.config.AgentxEntitlementProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEnvelopeRespVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class EnvelopeSignatureUtilTest {

    @Test
    void shouldSignAndVerifyAndFailAfterTampered() throws Exception {
        AgentxEntitlementProperties properties = new AgentxEntitlementProperties();
        properties.setSignatureSecret("ut-sign-secret");
        EnvelopeSignatureUtil util = new EnvelopeSignatureUtil();

        java.lang.reflect.Field field = EnvelopeSignatureUtil.class.getDeclaredField("entitlementProperties");
        field.setAccessible(true);
        field.set(util, properties);

        AccessEnvelopeRespVO.SystemEnforcedContext context = new AccessEnvelopeRespVO.SystemEnforcedContext();
        context.setUserId(1L);
        context.setChannelUserId("telegram:10001");
        context.setAgentId("dispatch-assistant");
        context.setAllowedActions(new ArrayList<String>() {{ add("route.read"); }});
        context.setResourceFilters(new HashMap<String, Object>() {{ put("region_codes", new ArrayList<String>() {{ add("east"); }}); }});
        context.setPolicyVersion("v2026.04.01");
        context.setDecisionId("dec_1");
        context.setIssuedAt(LocalDateTime.now());
        context.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        context.setSignature(util.sign(context));
        assertTrue(util.verify(context));

        context.getAllowedActions().add("admin.all");
        assertFalse(util.verify(context));
    }

}
