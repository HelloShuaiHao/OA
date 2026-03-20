package cn.iocoder.yudao.module.agentx.controller.admin.scenario;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigSaveReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import cn.iocoder.yudao.module.agentx.service.scenario.ScenarioConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AgentX 场景配置")
@RestController
@RequestMapping("/agentx/scenario")
@Validated
public class ScenarioConfigController {

    @Resource
    private ScenarioConfigService scenarioConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建场景配置")
    @PreAuthorize("@ss.hasPermission('agentx:scenario:create')")
    public CommonResult<Long> createScenarioConfig(@Valid @RequestBody AgentxScenarioConfigSaveReqVO createReqVO) {
        return success(scenarioConfigService.createScenarioConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改场景配置")
    @PreAuthorize("@ss.hasPermission('agentx:scenario:update')")
    public CommonResult<Boolean> updateScenarioConfig(@Valid @RequestBody AgentxScenarioConfigSaveReqVO updateReqVO) {
        scenarioConfigService.updateScenarioConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除场景配置")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:scenario:delete')")
    public CommonResult<Boolean> deleteScenarioConfig(@RequestParam("id") Long id) {
        scenarioConfigService.deleteScenarioConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得场景配置")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:scenario:query')")
    public CommonResult<AgentxScenarioConfigRespVO> getScenarioConfig(@RequestParam("id") Long id) {
        AgentxScenarioConfigDO config = scenarioConfigService.getScenarioConfig(id);
        return success(BeanUtils.toBean(config, AgentxScenarioConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得场景配置分页")
    @PreAuthorize("@ss.hasPermission('agentx:scenario:query')")
    public CommonResult<PageResult<AgentxScenarioConfigRespVO>> getScenarioConfigPage(@Valid AgentxScenarioConfigPageReqVO pageReqVO) {
        PageResult<AgentxScenarioConfigDO> pageResult = scenarioConfigService.getScenarioConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentxScenarioConfigRespVO.class));
    }

}
