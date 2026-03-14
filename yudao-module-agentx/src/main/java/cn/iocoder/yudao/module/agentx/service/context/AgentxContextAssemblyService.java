package cn.iocoder.yudao.module.agentx.service.context;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AgentX 上下文组装服务。
 */
public class AgentxContextAssemblyService {

    private final List<AgentxContextProvider> providers;
    private final AgentxContextVisibilityPolicy visibilityPolicy = new AgentxContextVisibilityPolicy();

    public AgentxContextAssemblyService(List<AgentxContextProvider> providers) {
        this.providers = providers;
    }

    public BusinessContextBundle assemble(AgentxContextRequest request) {
        Map<ContextLayer, Map<String, Object>> layers = new LinkedHashMap<>();
        Map<String, Object> seed = new HashMap<>(request.getSeedContext());
        layers.put(ContextLayer.REQUIRED, seed);
        List<ContextContribution> contributions = providers.stream()
                .map(provider -> provider.provide(request))
                .collect(Collectors.toList());
        contributions.forEach(contribution -> layers
                .computeIfAbsent(contribution.getLayer(), ignored -> new LinkedHashMap<>())
                .putAll(contribution.getValues()));
        ContextSnapshot snapshot = new ContextSnapshot()
                .setSnapshotId(UUID.randomUUID().toString())
                .setScenarioCode(request.getScenarioCode())
                .setBusinessKey(request.getBusinessKey())
                .setAssembledBy("agentx")
                .setAssembledAt("generated")
                .setRuleVersion("v1")
                .setSources(contributions.stream().map(ContextContribution::getSource).collect(Collectors.toList()))
                .setLayers(layers);
        Map<String, Object> runtimeContext = visibilityPolicy.filterForRuntime(snapshot);
        return new BusinessContextBundle(request.getScenarioCode(), runtimeContext)
                .setSnapshot(snapshot)
                .setSummaryContext(Map.of("contextSummary", String.join(",", runtimeContext.keySet())));
    }

}
