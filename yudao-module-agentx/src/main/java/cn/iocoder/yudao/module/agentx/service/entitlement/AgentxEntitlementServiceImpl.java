package cn.iocoder.yudao.module.agentx.service.entitlement;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.config.AgentxEntitlementProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigCreateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigUpdateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementObligationVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.entitlement.AgentxUserEntitlementDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.entitlement.AgentxUserEntitlementMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class AgentxEntitlementServiceImpl implements AgentxEntitlementService {

    @Resource
    private AgentxUserEntitlementMapper userEntitlementMapper;
    @Resource
    private AgentxEntitlementProperties entitlementProperties;

    @Override
    public Long createEntitlement(EntitlementConfigCreateReqVO reqVO) {
        AgentxUserEntitlementDO data = new AgentxUserEntitlementDO()
                .setUserId(reqVO.getUserId())
                .setAgentId(reqVO.getAgentId())
                .setDeptName(reqVO.getDeptName())
                .setJobTitle(reqVO.getJobTitle())
                .setRoleTags(toJson(reqVO.getRoleTags()))
                .setWorkRegion(reqVO.getWorkRegion())
                .setAllowedActions(toJson(reqVO.getAllowedActions()))
                .setResourceFilters(toJson(reqVO.getResourceFilters()))
                .setObligations(toJson(reqVO.getObligations()))
                .setPolicyVersion(nextPolicyVersion())
                .setEffectiveFrom(reqVO.getEffectiveFrom())
                .setEffectiveUntil(reqVO.getEffectiveUntil());
        userEntitlementMapper.insert(data);
        return data.getId();
    }

    @Override
    public void updateEntitlement(Long id, EntitlementConfigUpdateReqVO reqVO) {
        AgentxUserEntitlementDO exists = userEntitlementMapper.selectById(id);
        if (exists == null) {
            throw exception(ErrorCodeConstants.ENTITLEMENT_CONFIG_NOT_EXISTS);
        }
        AgentxUserEntitlementDO update = new AgentxUserEntitlementDO().setId(id);
        if (reqVO.getDeptName() != null) {
            update.setDeptName(reqVO.getDeptName());
        }
        if (reqVO.getJobTitle() != null) {
            update.setJobTitle(reqVO.getJobTitle());
        }
        if (reqVO.getRoleTags() != null) {
            update.setRoleTags(toJson(reqVO.getRoleTags()));
        }
        if (reqVO.getWorkRegion() != null) {
            update.setWorkRegion(reqVO.getWorkRegion());
        }
        if (reqVO.getAllowedActions() != null) {
            update.setAllowedActions(toJson(reqVO.getAllowedActions()));
        }
        if (reqVO.getResourceFilters() != null) {
            update.setResourceFilters(toJson(reqVO.getResourceFilters()));
        }
        if (reqVO.getObligations() != null) {
            update.setObligations(toJson(reqVO.getObligations()));
        }
        if (reqVO.getEffectiveFrom() != null) {
            update.setEffectiveFrom(reqVO.getEffectiveFrom());
        }
        if (reqVO.getEffectiveUntil() != null) {
            update.setEffectiveUntil(reqVO.getEffectiveUntil());
        }
        update.setPolicyVersion(nextPolicyVersion());
        userEntitlementMapper.updateById(update);
    }

    @Override
    public void deleteEntitlement(Long id) {
        if (userEntitlementMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.ENTITLEMENT_CONFIG_NOT_EXISTS);
        }
        userEntitlementMapper.deleteById(id);
    }

    @Override
    public List<EntitlementConfigRespVO> listEntitlements(Long userId, String agentId) {
        List<AgentxUserEntitlementDO> list = userEntitlementMapper.selectListByUserAndAgent(userId, agentId);
        List<EntitlementConfigRespVO> result = new ArrayList<>();
        for (AgentxUserEntitlementDO item : list) {
            EntitlementConfigRespVO vo = new EntitlementConfigRespVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setAgentId(item.getAgentId());
            vo.setDeptName(item.getDeptName());
            vo.setJobTitle(item.getJobTitle());
            vo.setRoleTags(parseStringList(item.getRoleTags()));
            vo.setWorkRegion(item.getWorkRegion());
            vo.setAllowedActions(parseStringList(item.getAllowedActions()));
            vo.setResourceFilters(parseMap(item.getResourceFilters()));
            vo.setObligations(parseObligations(item.getObligations()));
            vo.setPolicyVersion(item.getPolicyVersion());
            vo.setEffectiveFrom(item.getEffectiveFrom());
            vo.setEffectiveUntil(item.getEffectiveUntil());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    public AgentxUserEntitlementDO getEffectiveEntitlement(Long userId, String agentId) {
        return userEntitlementMapper.selectEffectiveByUserAndAgent(userId, agentId, LocalDateTime.now());
    }

    private String toJson(Object value) {
        return value == null ? null : JsonUtils.toJsonString(value);
    }

    private String nextPolicyVersion() {
        return entitlementProperties.getPolicyVersion() + "-" + System.currentTimeMillis();
    }

    private List<String> parseStringList(String json) {
        if (ObjectUtil.isEmpty(json)) {
            return new ArrayList<>();
        }
        return JsonUtils.parseArray(json, String.class);
    }

    private Map<String, Object> parseMap(String json) {
        if (ObjectUtil.isEmpty(json)) {
            return null;
        }
        return JsonUtils.parseObject(json, Map.class);
    }

    private List<EntitlementObligationVO> parseObligations(String json) {
        if (ObjectUtil.isEmpty(json)) {
            return new ArrayList<>();
        }
        return JsonUtils.parseArray(json, EntitlementObligationVO.class);
    }

}
