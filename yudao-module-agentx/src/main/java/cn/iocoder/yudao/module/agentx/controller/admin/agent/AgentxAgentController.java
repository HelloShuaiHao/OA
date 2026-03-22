package cn.iocoder.yudao.module.agentx.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentDetailRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentSaveReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentUpdateStatusReqVO;
import cn.iocoder.yudao.module.agentx.service.agent.AgentxAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AgentX 数字员工")
@RestController
@RequestMapping("/agentx/agent")
@Validated
public class AgentxAgentController {

    @Resource
    private AgentxAgentService agentService;

    @PostMapping("/create-draft")
    @Operation(summary = "创建数字员工（草稿）")
    @PreAuthorize("@ss.hasPermission('agentx:agent:create')")
    public CommonResult<Long> createDraft(@Valid @RequestBody AgentxAgentSaveReqVO reqVO) {
        return success(agentService.createAgent(reqVO, false));
    }

    @PostMapping("/create-publish")
    @Operation(summary = "创建数字员工并激活")
    @PreAuthorize("@ss.hasPermission('agentx:agent:create')")
    public CommonResult<Long> createPublish(@Valid @RequestBody AgentxAgentSaveReqVO reqVO) {
        return success(agentService.createAgent(reqVO, true));
    }

    @PutMapping("/update")
    @Operation(summary = "修改数字员工")
    @PreAuthorize("@ss.hasPermission('agentx:agent:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody AgentxAgentSaveReqVO reqVO) {
        agentService.updateAgent(reqVO);
        return success(true);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改数字员工状态")
    @PreAuthorize("@ss.hasPermission('agentx:agent:update')")
    public CommonResult<Boolean> updateStatus(@PathVariable("id") Long id,
                                              @Valid @RequestBody AgentxAgentUpdateStatusReqVO reqVO) {
        agentService.updateAgentStatus(id, reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数字员工")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:agent:delete')")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        agentService.deleteAgent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得数字员工详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:agent:query')")
    public CommonResult<AgentxAgentDetailRespVO> get(@RequestParam("id") Long id) {
        return success(agentService.getAgent(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得数字员工分页")
    @PreAuthorize("@ss.hasPermission('agentx:agent:query')")
    public CommonResult<PageResult<AgentxAgentRespVO>> page(@Valid AgentxAgentPageReqVO pageReqVO) {
        return success(agentService.getAgentPage(pageReqVO));
    }

}
