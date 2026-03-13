package cn.iocoder.yudao.module.agentx.service.tool;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Tool 调用数据域。
 */
@Data
@Accessors(chain = true)
public class AgentxDataScope {

    private Long tenantId;
    private List<Long> orgUnitIds;
    private List<String> businessKeys;
    private List<String> maskedFields;

    public String summarize() {
        return "tenant=" + tenantId + ",business=" + businessKeys;
    }

}
