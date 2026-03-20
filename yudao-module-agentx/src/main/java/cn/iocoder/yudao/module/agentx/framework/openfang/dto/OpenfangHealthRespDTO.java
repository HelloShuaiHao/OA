package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OpenFang 实例健康状态。
 */
@Data
@Accessors(chain = true)
public class OpenfangHealthRespDTO {

    /**
     * 是否在线。
     */
    private Boolean online;
    /**
     * OpenFang 版本。
     */
    private String version;
    /**
     * 诊断信息。
     */
    private String message;

}
