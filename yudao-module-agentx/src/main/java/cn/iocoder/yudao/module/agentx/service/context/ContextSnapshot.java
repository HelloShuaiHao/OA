package cn.iocoder.yudao.module.agentx.service.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ContextSnapshot {

    private String snapshotId;
    private String scenarioCode;
    private String businessKey;
    private String assembledBy;
    private String assembledAt;
    private String ruleVersion;
    private List<ContextSource> sources;
    private Map<ContextLayer, Map<String, Object>> layers;

}
