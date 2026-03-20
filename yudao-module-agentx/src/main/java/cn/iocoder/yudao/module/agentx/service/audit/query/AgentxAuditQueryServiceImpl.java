package cn.iocoder.yudao.module.agentx.service.audit.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.audit.vo.AgentxAuditEventPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AgentxAuditQueryServiceImpl implements AgentxAuditQueryService {

    @Resource
    private AgentxAuditEventMapper auditEventMapper;

    @Override
    public PageResult<AgentxAuditEventDO> getAuditEventPage(AgentxAuditEventPageReqVO reqVO) {
        return auditEventMapper.selectPage(reqVO);
    }

    @Override
    public AgentxAuditEventDO getAuditEvent(Long id) {
        return auditEventMapper.selectById(id);
    }

}
