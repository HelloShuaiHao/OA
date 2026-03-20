package cn.iocoder.yudao.module.agentx.controller.admin.audit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.audit.vo.AgentxAuditEventPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.audit.vo.AgentxAuditEventRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.service.audit.query.AgentxAuditQueryService;
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

@Tag(name = "管理后台 - AgentX 审计")
@RestController
@RequestMapping("/agentx/audit")
@Validated
public class AgentxAuditController {

    @Resource
    private AgentxAuditQueryService auditQueryService;

    @GetMapping("/page")
    @Operation(summary = "获得 AgentX 审计分页")
    @PreAuthorize("@ss.hasPermission('agentx:audit:query')")
    public CommonResult<PageResult<AgentxAuditEventRespVO>> getAuditPage(@Valid AgentxAuditEventPageReqVO reqVO) {
        PageResult<AgentxAuditEventDO> pageResult = auditQueryService.getAuditEventPage(reqVO);
        return success(BeanUtils.toBean(pageResult, AgentxAuditEventRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 AgentX 审计详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:audit:query')")
    public CommonResult<AgentxAuditEventRespVO> getAudit(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(auditQueryService.getAuditEvent(id), AgentxAuditEventRespVO.class));
    }

}
