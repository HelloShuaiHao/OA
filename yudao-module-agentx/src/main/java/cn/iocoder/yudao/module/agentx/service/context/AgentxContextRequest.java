package cn.iocoder.yudao.module.agentx.service.context;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 上下文组装请求。
 */
@Data
@AllArgsConstructor
public class AgentxContextRequest {

    private String scenarioCode;
    private String businessKey;
    private Map<String, Object> seedContext;

}
