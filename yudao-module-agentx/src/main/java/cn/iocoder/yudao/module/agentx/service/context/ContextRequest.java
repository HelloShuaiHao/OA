package cn.iocoder.yudao.module.agentx.service.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.Map;

/**
 * 动态上下文请求。
 */
@Data
@Accessors(chain = true)
public class ContextRequest {

    private Long userId;
    private String scenarioCode;
    private String businessKey;
    private Map<String, Object> params = Collections.emptyMap();

}
