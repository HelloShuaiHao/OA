package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义 Tool 元数据结构和受控服务暴露边界。
 */
public class AgentxToolGovernance {

    public List<String> requiredMetadataTypes() {
        return List.of(
                "ToolDescriptor",
                "ToolSchema",
                "ToolPolicy",
                "ToolAdapter"
        );
    }

    public String exposureBoundary() {
        return "service-adapter-not-mapper-or-table";
    }

    public boolean allowsDirectMapperExposure() {
        return false;
    }

    public boolean allowsDirectTableExposure() {
        return false;
    }

}
