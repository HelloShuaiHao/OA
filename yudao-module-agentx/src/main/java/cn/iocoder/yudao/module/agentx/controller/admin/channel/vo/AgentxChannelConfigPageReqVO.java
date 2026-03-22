package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 渠道配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentxChannelConfigPageReqVO extends PageParam {

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "状态")
    private Integer status;

}
