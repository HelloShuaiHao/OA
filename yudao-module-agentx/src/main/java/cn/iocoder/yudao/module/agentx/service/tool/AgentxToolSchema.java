package cn.iocoder.yudao.module.agentx.service.tool;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Tool 输入输出 schema 摘要。
 */
@Data
@Accessors(chain = true)
public class AgentxToolSchema {

    private String schemaType;
    private List<String> requiredFields;

}
