package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AgentX 审批桥服务。
 */
@Service
public class AgentxApprovalBridgeServiceImpl implements AgentxApprovalBridgeService {

    private static final int DECISION_APPROVED = 10;
    private static final int DECISION_REJECTED = 20;
    private static final String DEFAULT_LEAVE_PROCESS_KEY = "oa_leave";
    private static final Pattern USER_ID_PATTERN = Pattern.compile("(\\d+)$");

    private final AgentxApprovalBpmMapper bpmMapper = new AgentxApprovalBpmMapper();
    private final AgentxApprovalResolutionPolicy resolutionPolicy = new AgentxApprovalResolutionPolicy();

    @Resource
    private AgentxApprovalBindingMapper approvalBindingMapper;
    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;
    @Resource
    private OpenfangRuntimeBridge runtimeBridge;
    @Resource
    private BpmProcessInstanceService bpmProcessInstanceService;

    @Override
    public AgentxApprovalRequest buildApprovalRequest(AgentxTaskProjectionDO projection, String approvalId) {
        OpenfangApprovalDetailRespDTO detail = runtimeBridge.getApprovalDetail(projection.getOpenfangTaskRunId(), approvalId);
        return new AgentxApprovalRequest()
                .setScenarioCode(projection.getScenarioCode())
                .setBusinessKey(projection.getBusinessKey())
                .setOpenfangTaskRunId(detail.getTaskRunId())
                .setOpenfangApprovalId(detail.getApprovalId())
                .setTitle(detail.getTitle())
                .setReason(detail.getReason())
                .setRiskLevel(detail.getRiskLevel())
                .setActionSummary(detail.getActionSummary())
                .setRequesterId(detail.getRequesterId());
    }

    @Override
    public AgentxApprovalBindingDO createApprovalBinding(AgentxTaskProjectionDO projection,
                                                         AgentxApprovalRequest request,
                                                         String bpmProcessInstanceId) {
        AgentxApprovalBindingDO binding = new AgentxApprovalBindingDO();
        binding.setScenarioCode(projection.getScenarioCode());
        binding.setBusinessKey(projection.getBusinessKey());
        binding.setOpenfangTaskRunId(request.getOpenfangTaskRunId());
        binding.setOpenfangApprovalId(request.getOpenfangApprovalId());
        binding.setBpmProcessInstanceId(bpmProcessInstanceId);
        binding.setRiskLevel(request.getRiskLevel());
        binding.setCallbackRetryCount(0);
        binding.setCallbackFailed(false);
        binding.setActionSummary(request.getActionSummary());
        approvalBindingMapper.insert(binding);
        return binding;
    }

    @Override
    public AgentxApprovalBindingDO createApprovalBinding(AgentxTaskProjectionDO projection,
                                                         OpenfangApprovalDetailRespDTO detail,
                                                         String bpmProcessInstanceId) {
        return createApprovalBinding(projection, new AgentxApprovalRequest()
                .setScenarioCode(projection.getScenarioCode())
                .setBusinessKey(projection.getBusinessKey())
                .setOpenfangTaskRunId(detail.getTaskRunId())
                .setOpenfangApprovalId(detail.getApprovalId())
                .setTitle(detail.getTitle())
                .setReason(detail.getReason())
                .setRiskLevel(detail.getRiskLevel())
                .setActionSummary(detail.getActionSummary())
                .setRequesterId(detail.getRequesterId()), bpmProcessInstanceId);
    }

    @Override
    public AgentxBpmApprovalCreateReq buildBpmCreateRequest(AgentxApprovalRequest request) {
        return bpmMapper.toCreateRequest(request);
    }

    @Override
    public String createBpmProcessInstance(AgentxApprovalRequest request) {
        AgentxBpmApprovalCreateReq bpmReq = buildBpmCreateRequest(request);
        BpmProcessInstanceCreateReqDTO createReq = new BpmProcessInstanceCreateReqDTO();
        createReq.setProcessDefinitionKey(resolveProcessDefinitionKey(request.getScenarioCode()));
        createReq.setBusinessKey(request.getBusinessKey());
        createReq.setVariables(buildBpmVariables(bpmReq));
        return bpmProcessInstanceService.createProcessInstance(resolveStarterUserId(request.getRequesterId()), createReq);
    }

    @Override
    public void syncDecision(Long approvalBindingId, Long taskProjectionId, ApprovalDecision decision) {
        approvalBindingMapper.updateById(new AgentxApprovalBindingDO().setId(approvalBindingId)
                .setDecisionStatus(toDecisionStatus(decision)));
        taskProjectionMapper.updateById(new AgentxTaskProjectionDO().setId(taskProjectionId)
                .setProjectionStatus(toProjectionStatus(decision)));
    }

    @Override
    public AgentxApprovalResolution resolveOutcome(Long approvalBindingId, Long taskProjectionId, AgentxApprovalOutcome outcome) {
        AgentxApprovalResolution resolution = resolutionPolicy.resolve(outcome);
        approvalBindingMapper.updateById(new AgentxApprovalBindingDO().setId(approvalBindingId)
                .setDecisionStatus(resolution.getDecisionStatus()));
        taskProjectionMapper.updateById(new AgentxTaskProjectionDO().setId(taskProjectionId)
                .setProjectionStatus(resolution.getProjectionStatus()));
        return resolution;
    }

    private Integer toDecisionStatus(ApprovalDecision decision) {
        return decision == ApprovalDecision.APPROVED ? DECISION_APPROVED : DECISION_REJECTED;
    }

    private Integer toProjectionStatus(ApprovalDecision decision) {
        return decision == ApprovalDecision.APPROVED
                ? AgentxTaskProjectionStatusEnum.APPROVED.getStatus()
                : AgentxTaskProjectionStatusEnum.REJECTED.getStatus();
    }

    private String resolveProcessDefinitionKey(String scenarioCode) {
        if ("oa.leave.approval".equals(scenarioCode)) {
            return DEFAULT_LEAVE_PROCESS_KEY;
        }
        throw new IllegalArgumentException("Unsupported scenarioCode for BPM process creation: " + scenarioCode);
    }

    private Long resolveStarterUserId(String requesterId) {
        if (requesterId == null) {
            throw new IllegalArgumentException("requesterId is required to create BPM process");
        }
        Matcher matcher = USER_ID_PATTERN.matcher(requesterId);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new IllegalArgumentException("Cannot parse requesterId to userId: " + requesterId);
    }

    private Map<String, Object> buildBpmVariables(AgentxBpmApprovalCreateReq bpmReq) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", bpmReq.getTitle());
        variables.put("summary", bpmReq.getSummary());
        variables.put("approverSource", bpmReq.getApproverSource());
        variables.put("approverRef", bpmReq.getApproverRef());
        variables.put("scenarioCode", bpmReq.getScenarioCode());
        variables.put("taskRunId", bpmReq.getTaskRunId());
        variables.put("approvalId", bpmReq.getApprovalId());
        variables.put("riskLevel", bpmReq.getRiskLevel());
        variables.put("requesterId", bpmReq.getRequesterId());
        return variables;
    }

}
