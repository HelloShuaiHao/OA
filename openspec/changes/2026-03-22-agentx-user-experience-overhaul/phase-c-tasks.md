# Phase C：扩展能力

**工期：** 3-4 周
**优先级：** P1
**目标：** 增强 AI 能力和系统可靠性

---

## 验收标准

- [ ] 支持 AI 决策节点（Flowable 扩展）
- [ ] 支持 Tool 调用节点
- [ ] 异步处理提升性能（消息队列）
- [ ] OpenFang 故障时可降级
- [ ] 系统可用性 > 99%

---

## 任务清单

### Task 1: Flowable 扩展节点开发

**工作量：** 8 天
**负责人：** 后端开发

#### 子任务

1.1 **AI 决策节点**
```java
public class AiDecisionDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String prompt = getFieldValue("prompt", execution);
        Map<String, Object> variables = getInputVariables(execution);

        // 调用 LLM
        String decision = llmService.decide(prompt, variables);

        // 设置输出
        execution.setVariable("aiDecision", decision);
    }
}
```

**验收标准：**
- [ ] 节点可以正常执行
- [ ] 支持超时控制（30s）
- [ ] 异常不会导致流程卡死
- [ ] 有单元测试

**工作量：** 3 天

1.2 **Tool 调用节点**
```java
public class ToolCallDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String toolName = getFieldValue("toolName", execution);
        Map<String, Object> params = getParams(execution);

        // 权限检查
        checkToolPermission(execution, toolName);

        // 调用 Tool
        Object result = toolRegistry.invoke(toolName, params);

        execution.setVariable("toolResult", result);
    }
}
```

**验收标准：**
- [ ] 节点可以调用注册的 Tool
- [ ] 权限检查正确
- [ ] 参数类型转换正确
- [ ] 有异常处理和重试

**工作量：** 2 天

1.3 **数据查询节点**
- 查询 OA 系统数据
- 支持 SQL 查询和 API 调用

**验收标准：**
- [ ] 可以查询用户、部门、业务数据
- [ ] 有 SQL 注入防护
- [ ] 查询结果可以传递给下游节点

**工作量：** 1 天

1.4 **外部 API 节点**
- 调用外部 HTTP API
- 支持 GET/POST/PUT/DELETE

**验收标准：**
- [ ] 支持常见 HTTP 方法
- [ ] 支持超时和重试
- [ ] 支持认证（Bearer Token, Basic Auth）

**工作量：** 2 天

---

### Task 2: 流程设计器集成

**工作量：** 2 天
**负责人：** 前端开发

#### 子任务

2.1 **在 Flowable 设计器中注册扩展节点**
- 注册 4 种扩展节点
- 配置节点图标和属性面板

**验收标准：**
- [ ] 扩展节点在设计器中可见
- [ ] 可以拖拽到画布
- [ ] 属性面板可以配置参数

2.2 **节点配置界面**
- AI 决策节点：配置 Prompt 和输入变量
- Tool 调用节点：选择 Tool 和配置参数
- 数据查询节点：配置查询语句
- 外部 API 节点：配置 URL 和请求参数

**验收标准：**
- [ ] 配置界面友好
- [ ] 支持参数验证
- [ ] 有帮助文档

---

### Task 3: 消息队列引入

**工作量：** 3 天
**负责人：** 后端开发

#### 子任务

3.1 **集成 RabbitMQ**
- 配置 RabbitMQ 连接
- 创建交换机和队列

**验收标准：**
- [ ] RabbitMQ 连接正常
- [ ] 队列创建成功

3.2 **异步事件处理**
- 任务创建事件
- 任务状态变更事件
- 审批请求事件
- 审批完成事件

**验收标准：**
- [ ] 事件可以发送到队列
- [ ] 消费者可以处理事件
- [ ] 失败有重试机制

3.3 **改造现有同步调用**
- 将部分同步调用改为异步
- 保持关键路径同步

**验收标准：**
- [ ] 性能提升明显
- [ ] 不影响用户体验
- [ ] 有监控指标

---

### Task 4: 熔断降级机制

**工作量：** 2 天
**负责人：** 后端开发

#### 子任务

4.1 **集成 Resilience4j**
- 配置熔断器
- 配置降级策略

**验收标准：**
- [ ] OpenFang 调用有熔断保护
- [ ] 熔断后可以降级
- [ ] 恢复后自动重连

4.2 **降级策略**
- OpenFang 不可用时使用 Flowable
- 复杂 AI 任务降级为简单规则

**验收标准：**
- [ ] 降级逻辑正确
- [ ] 用户体验不受影响
- [ ] 有降级日志

---

### Task 5: 性能优化

**工作量：** 3 天
**负责人：** 后端开发

#### 子任务

5.1 **角色解析缓存优化**
- Redis 缓存
- 缓存预热

**验收标准：**
- [ ] 缓存命中率 > 80%
- [ ] 响应时间 < 100ms

5.2 **数据库查询优化**
- 增加索引
- 优化慢查询

**验收标准：**
- [ ] 慢查询 < 100ms
- [ ] 数据库连接池使用率 < 80%

5.3 **并行处理优化**
- 上下文组装并行化
- 使用 CompletableFuture

**验收标准：**
- [ ] 任务创建时间降低 50%

---

### Task 6: 测试和文档

**工作量：** 3 天
**负责人：** 测试 + 技术写作

#### 子任务

6.1 **测试**
- 扩展节点单元测试
- 流程执行集成测试
- 性能测试
- 压力测试

**验收标准：**
- [ ] 单元测试覆盖率 > 70%
- [ ] 集成测试通过
- [ ] 性能达标

6.2 **文档**
- 扩展节点开发指南
- 流程设计最佳实践
- 性能优化指南

**验收标准：**
- [ ] 文档完整
- [ ] 有示例代码

---

## Phase C 总结

**总工作量：** 21 天（4 周）

**关键里程碑：**
- Week 1: Flowable 扩展节点开发
- Week 2: 流程设计器集成 + 消息队列
- Week 3: 熔断降级 + 性能优化
- Week 4: 测试 + 文档

**Phase C 完成后：**
- 支持 AI 决策和 Tool 调用
- 系统性能和可靠性大幅提升
- 为生产环境做好准备
