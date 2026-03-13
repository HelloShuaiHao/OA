package cn.iocoder.yudao.module.agentx.service.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * 授权求交结果快照。
 */
@Data
@AllArgsConstructor
public class AgentxAuthorizationSnapshot {

    private Set<AgentxCapability> effectiveCapabilities;

}
