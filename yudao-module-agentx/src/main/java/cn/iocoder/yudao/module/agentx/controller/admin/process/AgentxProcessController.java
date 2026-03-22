package cn.iocoder.yudao.module.agentx.controller.admin.process;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessDefinitionDetailRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessDefinitionPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessDefinitionRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessSelectionReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessSelectionRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessStartReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxProcessStartRespVO;
import cn.iocoder.yudao.module.agentx.service.process.AgentxProcessExecutionService;
import cn.iocoder.yudao.module.agentx.service.process.AgentxProcessSelectionService;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AgentX 流程选择")
@RestController
@RequestMapping("/agentx/process")
@Validated
@Slf4j
public class AgentxProcessController {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmCategoryService categoryService;
    @Resource
    private AgentxProcessSelectionService processSelectionService;
    @Resource
    private AgentxProcessExecutionService processExecutionService;

    @GetMapping("/definition-page")
    @Operation(summary = "获得可选流程定义分页（最新版本）")
    @PreAuthorize("@ss.hasPermission('agentx:scenario:query')")
    public CommonResult<PageResult<AgentxProcessDefinitionRespVO>> getDefinitionPage(@Valid AgentxProcessDefinitionPageReqVO pageReqVO) {
        try {
            ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionTenantId(FlowableUtils.getTenantId())
                    .latestVersion();
            if (pageReqVO.getActiveOnly() == null || pageReqVO.getActiveOnly()) {
                query.active();
            }
            if (pageReqVO.getName() != null && !pageReqVO.getName().trim().isEmpty()) {
                query.processDefinitionNameLike("%" + pageReqVO.getName().trim() + "%");
            }
            if (pageReqVO.getKey() != null && !pageReqVO.getKey().trim().isEmpty()) {
                query.processDefinitionKey(pageReqVO.getKey().trim());
            }

            long count = query.count();
            if (count == 0) {
                return success(PageResult.empty(count));
            }

            List<ProcessDefinition> definitions = query.orderByProcessDefinitionName().asc()
                    .listPage(PageUtils.getStart(pageReqVO), pageReqVO.getPageSize());

            Map<String, BpmCategoryDO> categoryMap = Collections.emptyMap();
            if (CollUtil.isNotEmpty(definitions)) {
                try {
                    categoryMap = categoryService.getCategoryMap(convertSet(definitions, ProcessDefinition::getCategory));
                } catch (Exception ex) {
                    log.warn("[getDefinitionPage][load category map failed]", ex);
                }
            }

            List<AgentxProcessDefinitionRespVO> list = convertList(definitions, pd -> {
                AgentxProcessDefinitionRespVO vo = new AgentxProcessDefinitionRespVO();
                vo.setId(pd.getId());
                vo.setKey(pd.getKey());
                vo.setName(pd.getName());
                vo.setCategory(pd.getCategory());
                vo.setVersion(pd.getVersion());
                BpmCategoryDO category = categoryMap.get(pd.getCategory());
                vo.setCategoryName(category == null ? null : category.getName());
                return vo;
            });
            return success(new PageResult<>(list, count));
        } catch (Exception ex) {
            log.error("[getDefinitionPage][query process definitions failed]", ex);
            return success(PageResult.empty());
        }
    }

    @GetMapping("/definition-get")
    @Operation(summary = "获得流程定义详情（含 BPMN XML）")
    @Parameter(name = "id", required = true, description = "流程定义 ID")
    @PreAuthorize("@ss.hasPermission('agentx:scenario:query')")
    public CommonResult<AgentxProcessDefinitionDetailRespVO> getDefinition(@RequestParam("id") String id) {
        try {
            ProcessDefinition pd = repositoryService.getProcessDefinition(id);
            if (pd == null) {
                return success(null);
            }

            AgentxProcessDefinitionDetailRespVO vo = new AgentxProcessDefinitionDetailRespVO();
            vo.setId(pd.getId());
            vo.setKey(pd.getKey());
            vo.setName(pd.getName());
            vo.setCategory(pd.getCategory());
            vo.setVersion(pd.getVersion());
            vo.setBpmnXml(BpmnModelUtils.getBpmnXml(repositoryService.getProcessModel(id)));
            return success(vo);
        } catch (Exception ex) {
            log.error("[getDefinition][load process definition failed][id={}]", id, ex);
            return success(null);
        }
    }

    @PostMapping("/select")
    @Operation(summary = "按规则/自动策略选择流程")
    @PreAuthorize("@ss.hasPermission('agentx:agent:query')")
    public CommonResult<AgentxProcessSelectionRespVO> selectProcess(@Valid @RequestBody AgentxProcessSelectionReqVO reqVO) {
        AgentxProcessSelectionService.SelectionResult result = processSelectionService
                .selectProcess(reqVO.getAgentId(), reqVO.getSelectionMode(), reqVO.getContext());
        AgentxProcessSelectionRespVO respVO = new AgentxProcessSelectionRespVO();
        respVO.setProcessDefinitionId(result.getProcessDefinitionId());
        respVO.setProcessDefinitionKey(result.getProcessDefinitionKey());
        respVO.setProcessName(result.getProcessName());
        respVO.setReason(result.getReason());
        return success(respVO);
    }

    @PostMapping("/start")
    @Operation(summary = "选择并启动流程")
    @PreAuthorize("@ss.hasPermission('agentx:agent:update')")
    public CommonResult<AgentxProcessStartRespVO> startProcess(@Valid @RequestBody AgentxProcessStartReqVO reqVO) {
        String processInstanceId = processExecutionService.startSelectedProcess(
                reqVO.getAgentId(), reqVO.getSelectionMode(), reqVO.getBusinessKey(), reqVO.getContext());
        AgentxProcessStartRespVO respVO = new AgentxProcessStartRespVO();
        respVO.setProcessInstanceId(processInstanceId);
        return success(respVO);
    }

}
