package cn.iocoder.yudao.module.agentx.controller.admin.template;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.agentx.controller.admin.template.vo.AgentxTemplateRespVO;
import cn.iocoder.yudao.module.agentx.service.template.AgentxTemplateService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AgentX 模板库")
@RestController
@RequestMapping("/agentx/template")
@Validated
public class AgentxTemplateController {

    @Resource
    private AgentxTemplateService templateService;

    @GetMapping("/list")
    @Operation(summary = "获得模板列表")
    @PreAuthorize("@ss.hasPermission('agentx:agent:query')")
    public CommonResult<List<AgentxTemplateRespVO>> getTemplateList() {
        return success(templateService.getTemplateList());
    }

    @GetMapping("/get")
    @Operation(summary = "获得模板详情")
    @Parameter(name = "type", required = true)
    @PreAuthorize("@ss.hasPermission('agentx:agent:query')")
    public CommonResult<AgentxTemplateRespVO> getTemplate(@RequestParam("type") String type) {
        return success(templateService.getTemplate(type));
    }

}
