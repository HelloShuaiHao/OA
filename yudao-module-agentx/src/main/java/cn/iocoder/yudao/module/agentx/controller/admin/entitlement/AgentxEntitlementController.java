package cn.iocoder.yudao.module.agentx.controller.admin.entitlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigCreateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementConfigUpdateReqVO;
import cn.iocoder.yudao.module.agentx.service.entitlement.AgentxEntitlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AgentX 权限配置")
@RestController
@RequestMapping("/agentx/entitlement")
@Validated
public class AgentxEntitlementController {

    @Resource
    private AgentxEntitlementService entitlementService;

    @PostMapping("/config")
    @Operation(summary = "创建权限配置")
    @PreAuthorize("@ss.hasPermission('agentx:channel:update')")
    public CommonResult<Long> createEntitlement(@Valid @RequestBody EntitlementConfigCreateReqVO reqVO) {
        return success(entitlementService.createEntitlement(reqVO));
    }

    @GetMapping("/config")
    @Operation(summary = "查询权限配置列表")
    @PreAuthorize("@ss.hasPermission('agentx:channel:query')")
    public CommonResult<List<EntitlementConfigRespVO>> listEntitlements(@RequestParam(value = "userId", required = false) Long userId,
                                                                         @RequestParam(value = "agentId", required = false) String agentId) {
        return success(entitlementService.listEntitlements(userId, agentId));
    }

    @PutMapping("/config/{id}")
    @Operation(summary = "更新权限配置")
    @PreAuthorize("@ss.hasPermission('agentx:channel:update')")
    public CommonResult<Boolean> updateEntitlement(@Parameter(name = "id", required = true)
                                                    @PathVariable("id") Long id,
                                                    @Valid @RequestBody EntitlementConfigUpdateReqVO reqVO) {
        entitlementService.updateEntitlement(id, reqVO);
        return success(true);
    }

    @DeleteMapping("/config/{id}")
    @Operation(summary = "删除权限配置")
    @PreAuthorize("@ss.hasPermission('agentx:channel:delete')")
    public CommonResult<Boolean> deleteEntitlement(@Parameter(name = "id", required = true)
                                                    @PathVariable("id") Long id) {
        entitlementService.deleteEntitlement(id);
        return success(true);
    }

}
