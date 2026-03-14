package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenfangApprovalQueryContractTest {

    @Test
    void shouldDefineApprovalQueryKeysFieldsIsolationAndFilters() {
        OpenfangApprovalQueryContract contract = new OpenfangApprovalQueryContract();

        assertEquals("approval_id + task_run_id", contract.getQueryKeys());
        assertEquals("tenant scoped bearer token", contract.getTenantIsolationRule());
        assertEquals("approval:read", contract.getRequiredPermission());
        assertEquals("approvalId, taskRunId, title, reason, riskLevel, actionSummary, requesterId",
                contract.getResponseFields());
        assertEquals("no pagination; exact query only", contract.getPaginationRule());
    }

}
