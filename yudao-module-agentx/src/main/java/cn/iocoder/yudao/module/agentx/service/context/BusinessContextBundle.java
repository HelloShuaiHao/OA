package cn.iocoder.yudao.module.agentx.service.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 业务上下文快照。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class BusinessContextBundle {

    private final String scenarioCode;
    private final Map<String, Object> context;
    private ContextSnapshot snapshot;
    private Map<String, Object> summaryContext;

    public BusinessContextBundle(String scenarioCode, Map<String, Object> context) {
        this.scenarioCode = scenarioCode;
        this.context = context;
    }

}
