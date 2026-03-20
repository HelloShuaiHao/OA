package cn.iocoder.yudao.module.agentx.dal.dataobject.instance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * OpenFang 实例配置。
 */
@TableName("agentx_openfang_instance")
@KeySequence("agentx_openfang_instance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxOpenfangInstanceDO extends BaseDO {

    @TableId
    private Long id;
    private Long tenantId;
    private String instanceName;
    private String endpoint;
    private String apiKeyEncrypted;
    private Integer status;
    private String version;
    private LocalDateTime lastHeartbeat;

}
