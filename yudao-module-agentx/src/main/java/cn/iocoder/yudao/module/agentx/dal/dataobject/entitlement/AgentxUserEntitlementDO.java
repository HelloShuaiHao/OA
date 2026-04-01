package cn.iocoder.yudao.module.agentx.dal.dataobject.entitlement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("agentx_user_entitlement")
@KeySequence("agentx_user_entitlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxUserEntitlementDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    private String agentId;
    private String deptName;
    private String jobTitle;
    private String roleTags;
    private String workRegion;
    private String allowedActions;
    private String resourceFilters;
    private String obligations;
    private String policyVersion;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveUntil;

}
