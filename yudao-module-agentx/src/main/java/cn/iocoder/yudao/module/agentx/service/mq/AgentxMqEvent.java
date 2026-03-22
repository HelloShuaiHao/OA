package cn.iocoder.yudao.module.agentx.service.mq;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * AgentX MQ 事件载体。
 */
@Data
@Accessors(chain = true)
public class AgentxMqEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;
    private String scenarioCode;
    private String businessKey;
    private String taskRunId;
    private LocalDateTime eventTime;
    private Map<String, Object> payload;

}
