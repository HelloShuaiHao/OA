package cn.iocoder.yudao.module.agentx.service.accessaudit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AccessAuditRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AgentxAccessAuditPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.accessaudit.AgentxAccessAuditDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.accessaudit.AgentxAccessAuditMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Validated
public class AgentxAccessAuditServiceImpl implements AgentxAccessAuditService {

    @Resource
    private AgentxAccessAuditMapper accessAuditMapper;

    @Override
    public void logAccessDecision(String decisionId, Long userId, String channelUserId, String agentId,
                                  String conversationScope, String decision, String denyReason, String policyVersion) {
        logAccessDecision(decisionId, userId, channelUserId, agentId, conversationScope,
                null, null, null, decision, denyReason, policyVersion);
    }

    @Override
    public void logAccessDecision(String decisionId, Long userId, String channelUserId, String agentId,
                                  String conversationScope, String action, String resourceType, String resourceId,
                                  String decision, String denyReason, String policyVersion) {
        AgentxAccessAuditDO data = new AgentxAccessAuditDO()
                .setDecisionId(decisionId)
                .setUserId(userId == null ? -1L : userId)
                .setChannelUserId(channelUserId)
                .setAgentId(agentId)
                .setConversationScope(conversationScope)
                .setAction(action)
                .setResourceType(resourceType)
                .setResourceId(resourceId)
                .setDecision(decision)
                .setDenyReason(denyReason)
                .setPolicyVersion(policyVersion)
                .setRequestTime(LocalDateTime.now());
        accessAuditMapper.insert(data);
    }

    @Override
    public PageResult<AccessAuditRespVO> getAuditPage(AgentxAccessAuditPageReqVO reqVO) {
        PageResult<AgentxAccessAuditDO> page = accessAuditMapper.selectPage(reqVO);
        return BeanUtils.toBean(page, AccessAuditRespVO.class);
    }

}
