package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户渠道绑定 Response VO")
@Data
public class AgentxUserChannelBindingRespVO {

    private Long id;
    private Long userId;
    private String channelType;
    private String channelUserId;
    private String channelUsername;
    private LocalDateTime bindTime;
    private LocalDateTime unbindTime;
    private Integer status;
    private LocalDateTime createTime;

}
