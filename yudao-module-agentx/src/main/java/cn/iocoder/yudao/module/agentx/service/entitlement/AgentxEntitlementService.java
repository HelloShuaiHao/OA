package cn.iocoder.yudao.module.agentx.service.entitlement;

import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigCreateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigUpdateReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.entitlement.AgentxUserEntitlementDO;

import java.util.List;

public interface AgentxEntitlementService {

    Long createEntitlement(EntitlementConfigCreateReqVO reqVO);

    void updateEntitlement(Long id, EntitlementConfigUpdateReqVO reqVO);

    void deleteEntitlement(Long id);

    List<EntitlementConfigRespVO> listEntitlements(Long userId, String agentId);

    AgentxUserEntitlementDO getEffectiveEntitlement(Long userId, String agentId);

}
