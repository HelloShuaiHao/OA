package cn.iocoder.yudao.module.agentx.controller.admin.instance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - OpenFang 实例测试连接 Request VO")
@Data
public class AgentxOpenfangInstanceTestReqVO {

    @Schema(description = "实例编号；传入时优先读取库内配置", example = "1")
    private Long id;

    @Schema(description = "实例地址", example = "http://localhost:4201")
    private String endpoint;

    @Schema(description = "API Key", example = "sk-xxx")
    private String apiKey;

}
