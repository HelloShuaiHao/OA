package cn.iocoder.yudao.module.agentx.service.template;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.template.vo.AgentxTemplateRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.template.AgentxAgentTemplateDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.template.AgentxAgentTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
@Validated
public class AgentxTemplateServiceImpl implements AgentxTemplateService {

    @Resource
    private AgentxAgentTemplateMapper templateMapper;

    @Override
    public List<AgentxTemplateRespVO> getTemplateList() {
        return convertList(templateMapper.selectEnabledList(), this::convert);
    }

    @Override
    public AgentxTemplateRespVO getTemplate(String templateType) {
        AgentxAgentTemplateDO template = templateMapper.selectByTemplateType(templateType);
        if (template == null) {
            return null;
        }
        return convert(template);
    }

    private AgentxTemplateRespVO convert(AgentxAgentTemplateDO template) {
        AgentxTemplateRespVO respVO = new AgentxTemplateRespVO();
        respVO.setTemplateType(template.getTemplateType());
        respVO.setTemplateName(template.getTemplateName());
        respVO.setDescription(template.getDescription());
        respVO.setIcon(template.getIcon());
        fillJsonFields(template, respVO);
        return respVO;
    }

    private void fillJsonFields(AgentxAgentTemplateDO from, AgentxTemplateRespVO to) {
        to.setDefaultCapabilities(parseArray(from.getDefaultCapabilities(), String.class));
        to.setRecommendedProcessKeys(parseArray(from.getRecommendedProcessKeys(), String.class));
        to.setDefaultRules(parseArray(from.getDefaultRules(), AgentxTemplateRespVO.RuleItem.class));
    }

    private <T> List<T> parseArray(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        List<T> list = JsonUtils.parseArray(json, clazz);
        return list == null ? Collections.emptyList() : list;
    }

}
