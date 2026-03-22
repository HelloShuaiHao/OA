package cn.iocoder.yudao.module.agentx.service.process;

import java.util.Map;

public interface AgentxProcessExecutionService {

    String startSelectedProcess(Long agentId, String selectionMode, String businessKey, Map<String, Object> context);

}
