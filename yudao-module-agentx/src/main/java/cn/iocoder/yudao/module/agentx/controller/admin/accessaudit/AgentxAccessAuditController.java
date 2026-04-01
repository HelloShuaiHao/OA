package cn.iocoder.yudao.module.agentx.controller.admin.accessaudit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AccessAuditRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AgentxAccessAuditPageReqVO;
import cn.iocoder.yudao.module.agentx.service.accessaudit.AgentxAccessAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AgentX 访问审计")
@RestController
@RequestMapping("/agentx/audit")
@Validated
public class AgentxAccessAuditController {

    @Resource
    private AgentxAccessAuditService accessAuditService;

    @GetMapping("/access-log/page")
    @Operation(summary = "分页查询访问审计日志")
    @PreAuthorize("@ss.hasPermission('agentx:audit:query')")
    public CommonResult<PageResult<AccessAuditRespVO>> getAccessAuditPage(@Valid AgentxAccessAuditPageReqVO reqVO) {
        return success(accessAuditService.getAuditPage(reqVO));
    }

}
