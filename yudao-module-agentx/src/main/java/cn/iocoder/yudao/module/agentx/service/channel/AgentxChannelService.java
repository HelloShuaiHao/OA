package cn.iocoder.yudao.module.agentx.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.*;

import java.util.List;

public interface AgentxChannelService {

    Long createChannelConfig(AgentxChannelConfigSaveReqVO reqVO);

    void updateChannelConfig(AgentxChannelConfigSaveReqVO reqVO);

    void deleteChannelConfig(Long id);

    AgentxChannelConfigRespVO getChannelConfig(Long id);

    PageResult<AgentxChannelConfigRespVO> getChannelConfigPage(AgentxChannelConfigPageReqVO pageReqVO);

    AgentxChannelTestRespVO testChannelConnection(AgentxChannelTestReqVO reqVO);

    AgentxChannelAccessEvaluateRespVO evaluateChannelAccess(AgentxChannelAccessEvaluateReqVO reqVO);

    void refreshRuntimeAccessByChannelType(String channelType);

    List<AgentxUserChannelBindingRespVO> getMyBindings(Long userId);

    void unbind(Long id, Long userId);

    void adminUnbind(Long id);

    PageResult<AgentxUserChannelBindingRespVO> getBindingPage(AgentxUserChannelBindingPageReqVO pageReqVO);

}
