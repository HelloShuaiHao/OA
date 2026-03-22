package cn.iocoder.yudao.module.agentx.service.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.scenario.AgentxScenarioConfigMapper;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * AgentX 上下文组装服务。
 */
@Service
@Slf4j
public class AgentxContextAssemblyService {

    private static final long PROVIDER_TIMEOUT_MS = 5000L;
    private static final long ASSEMBLY_TIMEOUT_MS = 15000L;

    @Resource
    private List<ContextProvider> providers;
    @Resource
    private AgentxScenarioConfigMapper scenarioConfigMapper;
    @Autowired(required = false)
    private AgentxMetricsService metricsService;

    private final AgentxContextVisibilityPolicy visibilityPolicy = new AgentxContextVisibilityPolicy();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BusinessContextBundle assemble(AgentxContextRequest request) {
        long start = System.nanoTime();
        boolean success = false;
        try {
            Map<ContextLayer, Map<String, Object>> layers = new LinkedHashMap<>();
            Map<String, Object> seed = new HashMap<>(request.getSeedContext());
            layers.put(ContextLayer.REQUIRED, seed);

            List<ProviderConfig> providerConfigs = resolveProviderConfigs(request.getScenarioCode());
            List<ContextContribution> contributions = assembleContributions(request, providerConfigs);

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
            success = true;
            return new BusinessContextBundle(request.getScenarioCode(), runtimeContext)
                    .setSnapshot(snapshot)
                    .setSummaryContext(Collections.singletonMap("contextSummary", String.join(",", runtimeContext.keySet())));
        } finally {
            if (metricsService != null) {
                metricsService.recordContextAssembly(System.nanoTime() - start, success);
            }
        }
    }

    private List<ContextContribution> assembleContributions(AgentxContextRequest request, List<ProviderConfig> providerConfigs) {
        if (CollUtil.isEmpty(providerConfigs)) {
            return Collections.emptyList();
        }

        Map<String, ContextProvider> providerMap = providers.stream()
                .collect(Collectors.toMap(ContextProvider::getType, item -> item, (a, b) -> a));
        List<CompletableFuture<ContextContribution>> futures = providerConfigs.stream()
                .map(providerConfig -> CompletableFuture.supplyAsync(() -> {
                    ContextProvider provider = providerMap.get(providerConfig.getType());
                    if (provider == null) {
                        log.warn("[assembleContributions][未找到 ContextProvider type={}]", providerConfig.getType());
                        return null;
                    }
                    return invokeProviderWithTimeout(provider, providerConfig, request);
                }))
                .collect(Collectors.toList());
        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            future.get(ASSEMBLY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            List<ContextContribution> result = new ArrayList<>();
            for (CompletableFuture<ContextContribution> item : futures) {
                ContextContribution contribution = item.join();
                if (contribution != null) {
                    result.add(contribution);
                }
            }
            return result;
        } catch (TimeoutException ex) {
            throw new IllegalStateException("上下文组装超时", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("上下文组装失败", ex);
        }
    }

    private ContextContribution invokeProviderWithTimeout(ContextProvider provider, ProviderConfig providerConfig,
                                                          AgentxContextRequest request) {
        ContextRequest providerRequest = new ContextRequest()
                .setUserId(request.getUserId())
                .setScenarioCode(request.getScenarioCode())
                .setBusinessKey(request.getBusinessKey())
                .setParams(providerConfig.getParams());

        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> provider.provide(providerRequest));
        try {
            Map<String, Object> value = future.get(PROVIDER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return new ContextContribution()
                    .setSource(mapSource(providerConfig.getType()))
                    .setLayer(isCritical(providerConfig.getType()) ? ContextLayer.REQUIRED : ContextLayer.OPTIONAL)
                    .setValues(Collections.singletonMap(providerConfig.getType(), value));
        } catch (TimeoutException ex) {
            if (isCritical(providerConfig.getType())) {
                throw new IllegalStateException("关键 Provider 超时: " + providerConfig.getType(), ex);
            }
            return new ContextContribution()
                    .setSource(mapSource(providerConfig.getType()))
                    .setLayer(ContextLayer.OPTIONAL)
                    .setValues(Collections.singletonMap(providerConfig.getType(), errorMap("timeout")));
        } catch (Exception ex) {
            if (isCritical(providerConfig.getType())) {
                throw new IllegalStateException("关键 Provider 调用失败: " + providerConfig.getType(), ex);
            }
            return new ContextContribution()
                    .setSource(mapSource(providerConfig.getType()))
                    .setLayer(ContextLayer.OPTIONAL)
                    .setValues(Collections.singletonMap(providerConfig.getType(), errorMap(ex.getMessage())));
        }
    }

    private List<ProviderConfig> resolveProviderConfigs(String scenarioCode) {
        AgentxScenarioConfigDO config = scenarioConfigMapper.selectByScenarioCode(scenarioCode);
        if (config == null || StrUtil.isBlank(config.getConfig())) {
            return Collections.emptyList();
        }
        try {
            JsonNode root = objectMapper.readTree(config.getConfig());
            JsonNode providersNode = root.get("contextProviders");
            if (providersNode == null || !providersNode.isArray()) {
                return Collections.emptyList();
            }
            List<ProviderConfig> result = new ArrayList<>();
            for (JsonNode node : providersNode) {
                String type = node.path("type").asText(null);
                if (StrUtil.isBlank(type)) {
                    continue;
                }
                Map<String, Object> params = objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() { });
                params.remove("type");
                result.add(new ProviderConfig(type, params));
            }
            return result;
        } catch (Exception ex) {
            throw new IllegalStateException("解析场景上下文配置失败", ex);
        }
    }

    private ContextSource mapSource(String type) {
        if ("bpm_tasks".equals(type)) {
            return ContextSource.BPM;
        }
        if ("user_profile".equals(type)) {
            return ContextSource.USER_PROFILE;
        }
        if ("api".equals(type)) {
            return ContextSource.API;
        }
        return ContextSource.SYSTEM;
    }

    private boolean isCritical(String type) {
        return "bpm_tasks".equals(type);
    }

    private Map<String, Object> errorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    private static class ProviderConfig {

        private final String type;
        private final Map<String, Object> params;

        private ProviderConfig(String type, Map<String, Object> params) {
            this.type = type;
            this.params = params;
        }

        public String getType() {
            return type;
        }

        public Map<String, Object> getParams() {
            return params;
        }

    }

}
