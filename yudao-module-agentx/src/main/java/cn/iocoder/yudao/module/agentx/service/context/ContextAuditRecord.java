package cn.iocoder.yudao.module.agentx.service.context;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ContextAuditRecord {

    private String snapshotId;
    private String scenarioCode;
    private String businessKey;
    private String assembledBy;
    private String assembledAt;
    private String ruleVersion;

}
