package cn.iocoder.yudao.module.agentx.service.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxAuditDesensitizeServiceTest {

    @Test
    void shouldMaskPasswordPhoneIdCardAndApiKey() {
        AgentxAuditDesensitizeService service = new AgentxAuditDesensitizeService();
        String masked = service.mask("password=abc123 phone=13800138000 id=110101199001011234 apiKey=sk-abcdef");

        assertFalse(masked.contains("abc123"));
        assertFalse(masked.contains("13800138000"));
        assertFalse(masked.contains("110101199001011234"));
        assertFalse(masked.contains("sk-abcdef"));
        assertTrue(masked.contains("password=***"));
        assertTrue(masked.contains("138****8000"));
        assertTrue(masked.contains("110101********1234"));
        assertTrue(masked.contains("apiKey=***"));
    }

}
