package cn.iocoder.yudao.module.agentx.service.context;

public class AgentxContextAuditPolicy {

    public ContextAuditRecord toRecord(ContextSnapshot snapshot) {
        return new ContextAuditRecord()
                .setSnapshotId(snapshot.getSnapshotId())
                .setScenarioCode(snapshot.getScenarioCode())
                .setBusinessKey(snapshot.getBusinessKey())
                .setAssembledBy(snapshot.getAssembledBy())
                .setAssembledAt(snapshot.getAssembledAt())
                .setRuleVersion(snapshot.getRuleVersion());
    }

}
