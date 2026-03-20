package cn.iocoder.yudao.module.agentx.controller.admin.instance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - OpenFang 实例新增/修改 Request VO")
@Data
public class AgentxOpenfangInstanceSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "实例名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "本地开发实例")
    @NotBlank(message = "实例名称不能为空")
    private String instanceName;

    @Schema(description = "实例地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://localhost:4201")
    @NotBlank(message = "实例地址不能为空")
    private String endpoint;

    @Schema(description = "API Key。修改时可留空表示不变", example = "sk-xxx")
    private String apiKey;

    @Schema(description = "状态，参见 CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
