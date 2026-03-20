package cn.iocoder.yudao.module.agentx.service.approval.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.approval.vo.AgentxApprovalBindingPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AgentxApprovalQueryServiceImpl implements AgentxApprovalQueryService {

    @Resource
    private AgentxApprovalBindingMapper approvalBindingMapper;

    @Override
    public PageResult<AgentxApprovalBindingDO> getApprovalBindingPage(AgentxApprovalBindingPageReqVO reqVO) {
        return approvalBindingMapper.selectPage(reqVO);
    }

    @Override
    public AgentxApprovalBindingDO getApprovalBinding(Long id) {
        return approvalBindingMapper.selectById(id);
    }

}
