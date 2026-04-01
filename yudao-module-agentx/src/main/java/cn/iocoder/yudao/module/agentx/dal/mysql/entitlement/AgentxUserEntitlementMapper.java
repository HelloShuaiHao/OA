package cn.iocoder.yudao.module.agentx.dal.mysql.entitlement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.entitlement.AgentxUserEntitlementDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgentxUserEntitlementMapper extends BaseMapperX<AgentxUserEntitlementDO> {

    default List<AgentxUserEntitlementDO> selectListByUserAndAgent(Long userId, String agentId) {
        return selectList(new LambdaQueryWrapperX<AgentxUserEntitlementDO>()
                .eqIfPresent(AgentxUserEntitlementDO::getUserId, userId)
                .eqIfPresent(AgentxUserEntitlementDO::getAgentId, agentId)
                .orderByDesc(AgentxUserEntitlementDO::getId));
    }

    default AgentxUserEntitlementDO selectEffectiveByUserAndAgent(Long userId, String agentId, LocalDateTime now) {
        AgentxUserEntitlementDO specific = selectOne(new LambdaQueryWrapperX<AgentxUserEntitlementDO>()
                .eq(AgentxUserEntitlementDO::getUserId, userId)
                .eq(AgentxUserEntitlementDO::getAgentId, agentId)
                .and(wrapper -> wrapper
                        .isNull(AgentxUserEntitlementDO::getEffectiveFrom)
                        .or().le(AgentxUserEntitlementDO::getEffectiveFrom, now))
                .and(wrapper -> wrapper
                        .isNull(AgentxUserEntitlementDO::getEffectiveUntil)
                        .or().ge(AgentxUserEntitlementDO::getEffectiveUntil, now))
                .orderByDesc(AgentxUserEntitlementDO::getId)
                .last("LIMIT 1"));
        if (specific != null) {
            return specific;
        }
        return selectOne(new LambdaQueryWrapperX<AgentxUserEntitlementDO>()
                .eq(AgentxUserEntitlementDO::getUserId, userId)
                .isNull(AgentxUserEntitlementDO::getAgentId)
                .and(wrapper -> wrapper
                        .isNull(AgentxUserEntitlementDO::getEffectiveFrom)
                        .or().le(AgentxUserEntitlementDO::getEffectiveFrom, now))
                .and(wrapper -> wrapper
                        .isNull(AgentxUserEntitlementDO::getEffectiveUntil)
                        .or().ge(AgentxUserEntitlementDO::getEffectiveUntil, now))
                .orderByDesc(AgentxUserEntitlementDO::getId)
                .last("LIMIT 1"));
    }

}
