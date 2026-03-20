package cn.iocoder.yudao.module.agentx.controller.admin.instance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstancePageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceSaveReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceTestReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceTestRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import cn.iocoder.yudao.module.agentx.service.instance.OpenfangInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OpenFang 实例")
@RestController
@RequestMapping("/agentx/instance")
@Validated
public class OpenfangInstanceController {

    @Resource
    private OpenfangInstanceService openfangInstanceService;

    @PostMapping("/create")
    @Operation(summary = "创建 OpenFang 实例")
    @PreAuthorize("@ss.hasPermission('agentx:instance:create')")
    public CommonResult<Long> createInstance(@Valid @RequestBody AgentxOpenfangInstanceSaveReqVO createReqVO) {
        return success(openfangInstanceService.createInstance(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改 OpenFang 实例")
    @PreAuthorize("@ss.hasPermission('agentx:instance:update')")
    public CommonResult<Boolean> updateInstance(@Valid @RequestBody AgentxOpenfangInstanceSaveReqVO updateReqVO) {
        openfangInstanceService.updateInstance(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 OpenFang 实例")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:instance:delete')")
    public CommonResult<Boolean> deleteInstance(@RequestParam("id") Long id) {
        openfangInstanceService.deleteInstance(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OpenFang 实例")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:instance:query')")
    public CommonResult<AgentxOpenfangInstanceRespVO> getInstance(@RequestParam("id") Long id) {
        AgentxOpenfangInstanceDO instance = openfangInstanceService.getInstance(id);
        return success(BeanUtils.toBean(instance, AgentxOpenfangInstanceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OpenFang 实例分页")
    @PreAuthorize("@ss.hasPermission('agentx:instance:query')")
    public CommonResult<PageResult<AgentxOpenfangInstanceRespVO>> getInstancePage(@Valid AgentxOpenfangInstancePageReqVO pageReqVO) {
        PageResult<AgentxOpenfangInstanceDO> pageResult = openfangInstanceService.getInstancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentxOpenfangInstanceRespVO.class));
    }

    @PostMapping("/test-connection")
    @Operation(summary = "测试 OpenFang 实例连接")
    @PreAuthorize("@ss.hasPermission('agentx:instance:test')")
    public CommonResult<AgentxOpenfangInstanceTestRespVO> testConnection(@Valid @RequestBody AgentxOpenfangInstanceTestReqVO reqVO) {
        return success(openfangInstanceService.testConnection(reqVO));
    }

}
