package cn.iocoder.yudao.module.agentx.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.*;
import cn.iocoder.yudao.module.agentx.service.channel.AgentxBindService;
import cn.iocoder.yudao.module.agentx.service.channel.AgentxChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AgentX 渠道与绑定")
@RestController
@RequestMapping("/agentx/channel")
@Validated
public class AgentxChannelController {

    @Resource
    private AgentxChannelService channelService;
    @Resource
    private AgentxBindService bindService;

    @PostMapping("/config/create")
    @Operation(summary = "创建渠道配置")
    @PreAuthorize("@ss.hasPermission('agentx:channel:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody AgentxChannelConfigSaveReqVO reqVO) {
        return success(channelService.createChannelConfig(reqVO));
    }

    @PutMapping("/config/update")
    @Operation(summary = "更新渠道配置")
    @PreAuthorize("@ss.hasPermission('agentx:channel:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody AgentxChannelConfigSaveReqVO reqVO) {
        channelService.updateChannelConfig(reqVO);
        return success(true);
    }

    @DeleteMapping("/config/delete")
    @Operation(summary = "删除渠道配置")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:channel:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        channelService.deleteChannelConfig(id);
        return success(true);
    }

    @GetMapping("/config/get")
    @Operation(summary = "获得渠道配置")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:channel:query')")
    public CommonResult<AgentxChannelConfigRespVO> getConfig(@RequestParam("id") Long id) {
        return success(channelService.getChannelConfig(id));
    }

    @GetMapping("/config/page")
    @Operation(summary = "获得渠道配置分页")
    @PreAuthorize("@ss.hasPermission('agentx:channel:query')")
    public CommonResult<PageResult<AgentxChannelConfigRespVO>> pageConfig(@Valid AgentxChannelConfigPageReqVO pageReqVO) {
        return success(channelService.getChannelConfigPage(pageReqVO));
    }

    @PostMapping("/config/test")
    @Operation(summary = "测试渠道连接")
    @PreAuthorize("@ss.hasPermission('agentx:channel:update')")
    public CommonResult<AgentxChannelTestRespVO> testConfig(@Valid @RequestBody AgentxChannelTestReqVO reqVO) {
        return success(channelService.testChannelConnection(reqVO));
    }

    @PostMapping("/bind/generate")
    @Operation(summary = "生成绑定链接")
    @PermitAll
    public CommonResult<AgentxBindGenerateRespVO> generateBindLink(@Valid @RequestBody AgentxBindGenerateReqVO reqVO) {
        return success(bindService.generateBindLink(reqVO));
    }

    @GetMapping("/bind/confirm")
    @Operation(summary = "确认绑定（登录后调用）")
    public CommonResult<AgentxBindConfirmRespVO> confirmBind(@RequestParam("token") String token) {
        return success(bindService.confirmBind(token, getLoginUserId()));
    }

    @GetMapping("/binding/my")
    @Operation(summary = "获取当前用户绑定列表")
    public CommonResult<List<AgentxUserChannelBindingRespVO>> myBindings() {
        return success(channelService.getMyBindings(getLoginUserId()));
    }

    @DeleteMapping("/binding/unbind")
    @Operation(summary = "解绑当前用户渠道")
    @Parameter(name = "id", required = true)
    public CommonResult<Boolean> unbind(@RequestParam("id") Long id) {
        channelService.unbind(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/binding/admin-unbind")
    @Operation(summary = "管理端 - 解绑渠道")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:channel:update')")
    public CommonResult<Boolean> adminUnbind(@RequestParam("id") Long id) {
        channelService.adminUnbind(id);
        return success(true);
    }

    @GetMapping("/binding/page")
    @Operation(summary = "管理端 - 绑定记录分页")
    @PreAuthorize("@ss.hasPermission('agentx:channel:query')")
    public CommonResult<PageResult<AgentxUserChannelBindingRespVO>> bindingPage(@Valid AgentxUserChannelBindingPageReqVO pageReqVO) {
        return success(channelService.getBindingPage(pageReqVO));
    }

}
