package cn.iocoder.yudao.module.agentx.service.process;

import cn.hutool.core.util.StrUtil;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Validated
public class AgentxProcessExecutionServiceImpl implements AgentxProcessExecutionService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private AgentxProcessSelectionService processSelectionService;

    @Override
    public String startSelectedProcess(Long agentId, String selectionMode, String businessKey, Map<String, Object> context) {
        AgentxProcessSelectionService.SelectionResult selected = processSelectionService
                .selectProcess(agentId, selectionMode, context);
        Map<String, Object> variables = new HashMap<>();
        if (context != null) {
            variables.putAll(context);
        }
        variables.put("agentId", agentId);
        variables.put("selectedProcessDefinitionId", selected.getProcessDefinitionId());
        variables.put("selectedProcessDefinitionKey", selected.getProcessDefinitionKey());
        variables.put("selectionReason", selected.getReason());

        ProcessInstance processInstance;
        if (StrUtil.isNotBlank(selected.getProcessDefinitionId())) {
            processInstance = runtimeService.startProcessInstanceById(selected.getProcessDefinitionId(), businessKey, variables);
        } else {
            processInstance = runtimeService.startProcessInstanceByKey(selected.getProcessDefinitionKey(), businessKey, variables);
        }
        return processInstance.getProcessInstanceId();
    }

}
