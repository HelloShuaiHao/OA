package cn.iocoder.yudao.module.agentx.service.channel;

import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindConfirmRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateRespVO;

public interface AgentxBindService {

    AgentxBindGenerateRespVO generateBindLink(AgentxBindGenerateReqVO reqVO);

    AgentxBindConfirmRespVO confirmBind(String token, Long userId);

}
