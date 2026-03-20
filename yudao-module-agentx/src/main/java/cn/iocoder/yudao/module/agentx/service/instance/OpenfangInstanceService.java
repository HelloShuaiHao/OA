package cn.iocoder.yudao.module.agentx.service.instance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstancePageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceSaveReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceTestReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceTestRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;

import javax.validation.Valid;
import java.util.List;

/**
 * OpenFang 实例管理 Service。
 */
public interface OpenfangInstanceService {

    Long createInstance(@Valid AgentxOpenfangInstanceSaveReqVO createReqVO);

    void updateInstance(@Valid AgentxOpenfangInstanceSaveReqVO updateReqVO);

    void deleteInstance(Long id);

    AgentxOpenfangInstanceDO getInstance(Long id);

    PageResult<AgentxOpenfangInstanceDO> getInstancePage(AgentxOpenfangInstancePageReqVO pageReqVO);

    AgentxOpenfangInstanceTestRespVO testConnection(@Valid AgentxOpenfangInstanceTestReqVO testReqVO);

    List<AgentxOpenfangInstanceDO> getAllInstances();

    void refreshHealthStatus();

}
