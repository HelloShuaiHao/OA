# AgentX Phase C 测试记录（阶段性）

## 后端编译

```bash
mvn -pl yudao-module-agentx -am -DskipTests compile
```

结果：通过

## 关键单测

```bash
mvn -pl yudao-module-agentx -am \
  -Dtest=AgentxContextAssemblyServiceTest,AgentxRoleResolverImplTest,OpenfangRuntimeBridgeProtectedTest,AgentxEventPublisherTest,AgentxEventConsumerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：通过（12 tests, 0 failures）

```bash
mvn -pl yudao-module-agentx -am \
  -Dtest=OpenfangRuntimeBridgeProtectedTest,AgentxEventPublisherTest,AgentxEventConsumerTest,AgentxTaskOrchestrationServiceTest,AgentxApprovalBridgeServiceImplTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：通过（20 tests, 0 failures）

```bash
mvn -pl yudao-module-agentx -am \
  -Dtest=AgentxEventPublisherTest,AgentxEventConsumerTest,AgentxContextAssemblyServiceTest,OpenfangRuntimeBridgeProtectedTest,AgentxApiMetricsFilterTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：通过（13 tests, 0 failures）

## 前端校验

```bash
cd yudao-ui/yudao-ui-admin-vue3
pnpm -s eslint \
  src/components/bpmnProcessDesigner/package/penal/custom-config/components/AgentxServiceTaskConfig.vue \
  src/components/bpmnProcessDesigner/package/designer/plugins/palette/CustomPalette.js \
  src/components/bpmnProcessDesigner/package/penal/custom-config/data.ts
```

结果：通过

## 运行期脚本验证

```bash
bash script/agentx/phase-c-api-probe.sh
bash script/agentx/phase-c-slo-check.sh
```

本地采样结果（2026-03-22）：

1. API 探测（120 次）
   - Availability: `100.00%`
   - Avg Latency: `6.477ms`
   - Max Latency: `36.271ms`
   - 结论：`PASS (>=99%)`

2. SLO 检查（Prometheus + MySQL）
   - Prometheus endpoint: `/admin-api/actuator/prometheus`
   - `agentx_api_availability = 1`（PASS）
   - `slow_query_max_avg_ms = 19.068`（PASS，<100ms）
   - `db_pool_usage_pct = 1.99`（PASS，<80%）
