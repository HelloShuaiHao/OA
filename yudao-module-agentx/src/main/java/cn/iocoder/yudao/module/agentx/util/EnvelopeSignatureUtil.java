package cn.iocoder.yudao.module.agentx.util;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.config.AgentxEntitlementProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEnvelopeRespVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class EnvelopeSignatureUtil {

    @Resource
    private AgentxEntitlementProperties entitlementProperties;

    public String sign(AccessEnvelopeRespVO.SystemEnforcedContext context) {
        String payload = buildCanonicalPayload(context);
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, entitlementProperties.getSignatureSecret().getBytes(StandardCharsets.UTF_8));
        return "sha256:" + mac.digestHex(payload);
    }

    public boolean verify(AccessEnvelopeRespVO.SystemEnforcedContext context) {
        if (context == null || context.getSignature() == null) {
            return false;
        }
        String expected = sign(context);
        return expected.equals(context.getSignature());
    }

    private String buildCanonicalPayload(AccessEnvelopeRespVO.SystemEnforcedContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", context.getUserId());
        data.put("channelUserId", context.getChannelUserId());
        data.put("agentId", context.getAgentId());
        data.put("allowedActions", context.getAllowedActions());
        data.put("resourceFilters", context.getResourceFilters());
        data.put("obligations", context.getObligations());
        data.put("policyVersion", context.getPolicyVersion());
        data.put("decisionId", context.getDecisionId());
        data.put("issuedAt", context.getIssuedAt());
        data.put("expiresAt", context.getExpiresAt());
        Object canonical = canonicalize(data);
        return JsonUtils.toJsonString(canonical);
    }

    @SuppressWarnings("unchecked")
    private Object canonicalize(Object value) {
        if (value instanceof Map) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            ((Map<String, Object>) value).forEach((k, v) -> sorted.put(k, canonicalize(v)));
            return sorted;
        }
        if (value instanceof List) {
            List<Object> list = new ArrayList<>();
            for (Object item : (List<Object>) value) {
                list.add(canonicalize(item));
            }
            return list;
        }
        return value;
    }

}
