# AgentX Phase C 性能优化说明

## 本轮优化点

1. 上下文组装并行化  
   文件：`AgentxContextAssemblyService`
   - 原实现按 provider 顺序执行，整体时延受最慢 provider 线性叠加影响。
   - 现实现改为 `CompletableFuture.allOf(...)` 并发执行 provider。
   - 保留单 provider 超时保护和关键 provider 失败拦截。

2. 角色解析缓存命中率可观测  
   文件：`AgentxRoleResolverImpl`、`AgentxMetricsService`
   - 在缓存命中/未命中路径埋点。
   - 新增指标：`agentx_role_resolve_cache_hit_rate`。

3. 上下文组装性能可观测  
   文件：`AgentxContextAssemblyService`、`AgentxMetricsService`
   - 新增上下文组装总耗时记录和成功率统计。
   - 新增指标：
     - `agentx_context_assembly_duration_seconds`
     - `agentx_context_assembly_success_rate`

4. API 可用性指标  
   文件：`AgentxApiMetricsFilter`、`AgentxMetricsService`
   - 记录 AgentX API 请求总数与成功数（`HTTP < 500` 记为成功）。
   - 新增指标：`agentx_api_availability`。

5. MQ 发布链路异步化  
   文件：`AgentxEventPublisher`、`AgentxConfiguration`、`AgentxMqProperties`
   - 事件发布改为线程池异步执行，主链路不再等待 MQ 发送重试完成。
   - 线程池拒绝时自动降级为同步发送，确保可靠性优先于吞吐。
   - 支持配置项：
     - `yudao.agentx.mq.async-publish`
     - `yudao.agentx.mq.publish-core-pool-size`
     - `yudao.agentx.mq.publish-max-pool-size`
     - `yudao.agentx.mq.publish-queue-capacity`

6. 数据库索引优化  
   文件：`V20260322_04__agentx_phase_c_perf_indexes.sql`
   - `agentx_task_projection` 新增：
     - `idx_projection_status_deleted`
     - `idx_tenant_create_time_deleted`
   - `agentx_approval_binding` 新增：
     - `idx_decision_status_deleted`

## 指标用途

- `agentx_role_resolve_cache_hit_rate`：用于验证缓存命中率是否达到目标（>80%）。
- `agentx_context_assembly_duration_seconds`：用于观察上下文构建时延分布。
- `agentx_context_assembly_success_rate`：用于观察上下文构建稳定性。
- `agentx_api_availability`：用于衡量 API 可用性目标（>99%）。

## 验证建议

1. 压测角色解析接口，观察命中率是否稳定高于 80%。
2. 压测任务创建链路，确认上下文组装 P95 时延下降。
3. 结合现有 `agentx_api_duration_seconds` 观察端到端收益。

## 运行期验收脚本

1. API 可用性探测（目标：>=99%）  
   脚本：`script/agentx/phase-c-api-probe.sh`
   ```bash
   BASE_URL=http://127.0.0.1:48080 \
   TARGET_PATH='/admin-api/agentx/task/page?pageNo=1&pageSize=10' \
   REQUESTS=300 \
   bash script/agentx/phase-c-api-probe.sh
   ```

2. SLO 与数据库指标检查（可用性/慢查询/连接池）  
   脚本：`script/agentx/phase-c-slo-check.sh`
   ```bash
   BASE_URL=http://127.0.0.1:48080 \
   DB_NAME=ruoyi-vue-pro \
   MYSQL_HOST=127.0.0.1 \
   MYSQL_PORT=3306 \
   MYSQL_USER=root \
   MYSQL_PASSWORD=123456 \
   bash script/agentx/phase-c-slo-check.sh
   ```

备注：
- 脚本会自动探测 Prometheus 端点（`/actuator/prometheus`、`/admin-api/actuator/prometheus` 等）。
- 慢查询统计只统计 AgentX 业务 DML（SELECT/UPDATE/INSERT/DELETE），不包含建表等 DDL。
