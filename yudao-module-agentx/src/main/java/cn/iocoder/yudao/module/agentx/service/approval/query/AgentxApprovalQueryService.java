package cn.iocoder.yudao.module.agentx.service.approval.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.approval.vo.AgentxApprovalBindingPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;

public interface AgentxApprovalQueryService {

    PageResult<AgentxApprovalBindingDO> getApprovalBindingPage(AgentxApprovalBindingPageReqVO reqVO);

    AgentxApprovalBindingDO getApprovalBinding(Long id);

}
