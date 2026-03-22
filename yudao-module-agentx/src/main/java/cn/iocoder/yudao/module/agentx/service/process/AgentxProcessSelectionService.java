package cn.iocoder.yudao.module.agentx.service.process;

import java.util.Map;

public interface AgentxProcessSelectionService {

    SelectionResult selectProcess(Long agentId, String selectionMode, Map<String, Object> context);

    class SelectionResult {
        private String processDefinitionId;
        private String processDefinitionKey;
        private String processName;
        private String reason;

        public String getProcessDefinitionId() {
            return processDefinitionId;
        }

        public SelectionResult setProcessDefinitionId(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public String getProcessDefinitionKey() {
            return processDefinitionKey;
        }

        public SelectionResult setProcessDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public String getProcessName() {
            return processName;
        }

        public SelectionResult setProcessName(String processName) {
            this.processName = processName;
            return this;
        }

        public String getReason() {
            return reason;
        }

        public SelectionResult setReason(String reason) {
            this.reason = reason;
            return this;
        }
    }

}
