package cn.iocoder.yudao.module.agentx.service.access;

import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEnvelopeRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEvaluateReqVO;

public interface AgentxAccessService {

    AccessEnvelopeRespVO evaluateAccess(AccessEvaluateReqVO reqVO);

    boolean verifyEnvelope(AccessEnvelopeRespVO.SystemEnforcedContext context);

}
