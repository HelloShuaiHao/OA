package cn.iocoder.yudao.module.agentx.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.approval.vo.AgentxApprovalBindingPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.approval.vo.AgentxApprovalBindingRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.service.approval.query.AgentxApprovalQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AgentX 审批")
@RestController
@RequestMapping("/agentx/approval")
@Validated
public class AgentxApprovalController {

    @Resource
    private AgentxApprovalQueryService approvalQueryService;

    @GetMapping("/page")
    @Operation(summary = "获得 AgentX 审批分页")
    @PreAuthorize("@ss.hasPermission('agentx:approval:query')")
    public CommonResult<PageResult<AgentxApprovalBindingRespVO>> getApprovalPage(@Valid AgentxApprovalBindingPageReqVO reqVO) {
        PageResult<AgentxApprovalBindingDO> pageResult = approvalQueryService.getApprovalBindingPage(reqVO);
        return success(BeanUtils.toBean(pageResult, AgentxApprovalBindingRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 AgentX 审批详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:approval:query')")
    public CommonResult<AgentxApprovalBindingRespVO> getApproval(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(approvalQueryService.getApprovalBinding(id), AgentxApprovalBindingRespVO.class));
    }

}
