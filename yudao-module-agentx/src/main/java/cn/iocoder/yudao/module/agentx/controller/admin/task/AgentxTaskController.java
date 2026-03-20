package cn.iocoder.yudao.module.agentx.controller.admin.task;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskCreateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskProjectionPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskProjectionRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskRuntimeRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.scenario.AgentxScenarioConfigMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxRiskLevelEnum;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import cn.iocoder.yudao.module.agentx.service.context.AgentxContextAssemblyService;
import cn.iocoder.yudao.module.agentx.service.context.AgentxContextRequest;
import cn.iocoder.yudao.module.agentx.service.context.BusinessContextBundle;
import cn.iocoder.yudao.module.agentx.service.task.query.AgentxTaskQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.obtainAuthorization;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.SCENARIO_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.SCENARIO_DISABLED;

@Tag(name = "管理后台 - AgentX 任务")
@RestController
@RequestMapping("/agentx/task")
@Validated
public class AgentxTaskController {

    @Resource
    private AgentxTaskQueryService taskQueryService;
    @Resource
    private AgentxScenarioConfigMapper scenarioConfigMapper;
    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;
    @Resource
    private AgentxContextAssemblyService contextAssemblyService;
    @Resource
    private OpenfangRuntimeBridge openfangRuntimeBridge;

    @PostMapping("/create")
    @Operation(summary = "创建并触发 AgentX 任务")
    @PreAuthorize("@ss.hasPermission('agentx:task:query')")
    public CommonResult<AgentxTaskProjectionRespVO> createTask(@Valid @RequestBody AgentxTaskCreateReqVO reqVO,
                                                               HttpServletRequest request) {
        if (StrUtil.isNotBlank(reqVO.getIdempotencyKey())) {
            AgentxTaskProjectionDO existing = taskProjectionMapper.selectByIdempotencyKey(reqVO.getIdempotencyKey());
            if (existing != null) {
                return success(BeanUtils.toBean(existing, AgentxTaskProjectionRespVO.class));
            }
        }
        AgentxScenarioConfigDO scenario = scenarioConfigMapper.selectByScenarioCode(reqVO.getScenarioCode());
        if (scenario == null) {
            throw exception(SCENARIO_CONFIG_NOT_EXISTS);
        }
        if (!Integer.valueOf(1).equals(scenario.getEnabled())) {
            throw exception(SCENARIO_DISABLED);
        }

        Long loginUserId = getLoginUserId();
        AgentxContextRequest contextRequest = new AgentxContextRequest()
                .setScenarioCode(reqVO.getScenarioCode())
                .setBusinessKey(reqVO.getBusinessKey())
                .setUserId(loginUserId)
                .setSeedContext(Collections.emptyMap());
        BusinessContextBundle contextBundle = contextAssemblyService.assemble(contextRequest);

        String idempotencyKey = StrUtil.blankToDefault(reqVO.getIdempotencyKey(), UUID.randomUUID().toString());
        OpenfangWorkflowRunReqDTO workflowRunReq = new OpenfangWorkflowRunReqDTO();
        workflowRunReq.setScenarioCode(reqVO.getScenarioCode());
        workflowRunReq.setBusinessKey(reqVO.getBusinessKey());
        workflowRunReq.setIdempotencyKey(idempotencyKey);
        workflowRunReq.setPrincipalType("ADMIN_USER");
        workflowRunReq.setPrincipalId(loginUserId == null ? null : String.valueOf(loginUserId));
        workflowRunReq.setBpmAccessToken(obtainAuthorization(request, "Authorization", "access_token"));
        workflowRunReq.setContextBundle(contextBundle.getContext());
        workflowRunReq.setContextSummary("context-keys=" + contextBundle.getContext().keySet());

        OpenfangWorkflowRunRespDTO runResp = openfangRuntimeBridge.runWorkflow(scenario.getOpenfangWorkflowId(), workflowRunReq);
        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setScenarioCode(reqVO.getScenarioCode());
        projection.setBusinessKey(reqVO.getBusinessKey());
        projection.setIdempotencyKey(idempotencyKey);
        projection.setOpenfangTaskRunId(runResp.getTaskRunId());
        projection.setProjectionStatus(AgentxTaskProjectionStatusEnum.RUNNING.getStatus());
        projection.setRiskLevel(AgentxRiskLevelEnum.MEDIUM.getLevel());
        taskProjectionMapper.insert(projection);
        return success(BeanUtils.toBean(projection, AgentxTaskProjectionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 AgentX 任务分页")
    @PreAuthorize("@ss.hasPermission('agentx:task:query')")
    public CommonResult<PageResult<AgentxTaskProjectionRespVO>> getTaskPage(@Valid AgentxTaskProjectionPageReqVO pageReqVO) {
        PageResult<AgentxTaskProjectionDO> pageResult = taskQueryService.getTaskProjectionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentxTaskProjectionRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 AgentX 任务详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:task:query')")
    public CommonResult<AgentxTaskProjectionRespVO> getTask(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(taskQueryService.getTaskProjection(id), AgentxTaskProjectionRespVO.class));
    }

    @GetMapping("/runtime")
    @Operation(summary = "获得 AgentX 运行时详情")
    @Parameter(name = "taskRunId", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:task:query')")
    public CommonResult<AgentxTaskRuntimeRespVO> getTaskRuntime(@RequestParam("taskRunId") String taskRunId) {
        OpenfangTaskRespDTO runtime = taskQueryService.getTaskRuntime(taskRunId);
        return success(BeanUtils.toBean(runtime, AgentxTaskRuntimeRespVO.class));
    }

}
