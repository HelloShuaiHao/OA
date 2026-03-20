package cn.iocoder.yudao.module.agentx.service.audit.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.audit.vo.AgentxAuditEventPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;

public interface AgentxAuditQueryService {

    PageResult<AgentxAuditEventDO> getAuditEventPage(AgentxAuditEventPageReqVO reqVO);

    AgentxAuditEventDO getAuditEvent(Long id);

}
