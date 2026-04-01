package cn.iocoder.yudao.module.agentx.service.accessaudit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AccessAuditRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AgentxAccessAuditPageReqVO;

public interface AgentxAccessAuditService {

    void logAccessDecision(String decisionId, Long userId, String channelUserId, String agentId,
                           String conversationScope, String decision, String denyReason, String policyVersion);

    void logAccessDecision(String decisionId, Long userId, String channelUserId, String agentId,
                           String conversationScope, String action, String resourceType, String resourceId,
                           String decision, String denyReason, String policyVersion);

    PageResult<AccessAuditRespVO> getAuditPage(AgentxAccessAuditPageReqVO reqVO);

}
