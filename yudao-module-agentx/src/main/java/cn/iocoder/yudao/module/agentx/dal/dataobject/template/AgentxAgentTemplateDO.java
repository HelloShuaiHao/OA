package cn.iocoder.yudao.module.agentx.dal.dataobject.template;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("agentx_agent_template")
@TenantIgnore
@KeySequence("agentx_agent_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxAgentTemplateDO extends BaseDO {

    @TableId
    private Long id;
    private String templateType;
    private String templateName;
    private String description;
    private String icon;
    private String defaultCapabilities;
    private String recommendedProcessKeys;
    private String defaultRules;
    private Boolean enabled;

}
