package cn.iocoder.yudao.module.agentx.dal.mysql.template;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.template.AgentxAgentTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxAgentTemplateMapper extends BaseMapperX<AgentxAgentTemplateDO> {

    default List<AgentxAgentTemplateDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<AgentxAgentTemplateDO>()
                .eq(AgentxAgentTemplateDO::getEnabled, true)
                .orderByAsc(AgentxAgentTemplateDO::getId));
    }

    default AgentxAgentTemplateDO selectByTemplateType(String templateType) {
        return selectOne(AgentxAgentTemplateDO::getTemplateType, templateType);
    }

}
