# Phase B：身份与渠道

**工期：** 3-4 周
**优先级：** P0
**目标：** 打通外部渠道和用户身份，实现一键绑定

---

## 验收标准

- [x] 外部渠道用户可被识别并绑定 OA 身份
- [x] 绑定流程 <= 1 分钟（一键链接）
- [x] 绑定关系永久有效，无需重复绑定
- [x] 支持多渠道绑定（Telegram + 企业微信 + 钉钉）
- [x] 关键指标可监控（Prometheus）

---

## 任务清单

### Task 1: 渠道配置 UI

**工作量：** 3 天
**负责人：** 前端开发

#### 子任务

1.1 **渠道配置页面**
- Telegram Bot 配置
- 企业微信配置
- 钉钉配置

**验收标准：**
- [x] 可以配置 Bot Token
- [x] 支持测试连接
- [x] 可以选择关联的 Agent
- [x] 可以配置访问控制（所有员工/指定部门/指定人员）

1.2 **在 Agent 详情页增加渠道配置**
- 每个 Agent 可以配置自己的 Bot
- 显示当前绑定的渠道

**验收标准：**
- [x] Agent 详情页有渠道配置入口
- [x] 可以查看和编辑渠道配置

---

### Task 2: 用户身份绑定流程

**工作量：** 5 天
**负责人：** 后端开发

#### 子任务

2.1 **绑定链接生成**
- 用户首次交互时生成绑定链接
- JWT Token 包含渠道信息
- Token 10 分钟有效（防止链接泄露）

**验收标准：**
- [x] 可以生成绑定链接
- [x] Token 包含必要信息（channel_type, channel_user_id）
- [x] Token 有签名防篡改
- [x] Token 10 分钟后过期

**技术实现：**
```java
@GetMapping("/agentx/bind")
public String bind(@RequestParam String token, HttpSession session) {
    // 1. 验证 token
    // 2. 检查用户是否已登录
    // 3. 创建绑定记录
    // 4. 通知 OpenFang
}
```

2.2 **绑定页面**
- 用户点击链接跳转到 OA 登录页
- 登录后自动完成绑定
- 显示绑定成功页面

**验收标准：**
- [x] 未登录用户自动跳转登录页
- [x] 登录后自动完成绑定
- [x] 绑定成功有友好提示
- [x] 绑定失败有清晰错误信息

2.3 **绑定管理**
- 用户可以在个人中心查看已绑定的渠道
- 支持解绑和重新绑定
- 管理员可以查看所有绑定关系

**验收标准：**
- [x] 个人中心显示已绑定渠道
- [x] 支持解绑操作
- [x] 管理员可以查看和管理绑定关系

---

### Task 3: 角色解析服务

**工作量：** 5 天
**负责人：** 后端开发

#### 子任务

3.1 **实现 RoleResolver 接口**
```java
public interface RoleResolver {
    List<Long> resolveRole(String roleType, Map<String, Object> context);
}
```

支持的角色类型：
- `direct_manager` - 直属上级
- `department_head` - 部门负责人
- `department_vp` - 部门副总
- `hr_manager` - HR 经理
- `finance_approver` - 财务审批人
- `fixed_role` - 固定角色
- `custom_role` - 自定义角色

**验收标准：**
- [x] 所有角色类型都有实现
- [x] 查询组织架构数据正确
- [x] 找不到审批人时有降级策略
- [x] 有单元测试覆盖

3.2 **缓存优化**
- 角色解析结果缓存 5 分钟
- 使用 Redis 缓存

**验收标准：**
- [x] 缓存命中率 > 80%
- [x] 响应时间 < 100ms（有缓存）
- [x] 响应时间 < 500ms（无缓存）

---

### Task 4: 数据库表

**工作量：** 1 天
**负责人：** 后端开发

#### 子任务

4.1 **创建表**
```sql
-- 渠道配置表
CREATE TABLE agentx_channel_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    channel_type VARCHAR(32) NOT NULL,
    channel_name VARCHAR(128),
    config JSON,
    status TINYINT DEFAULT 1,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL
);

-- 渠道-Agent 关联表
CREATE TABLE agentx_channel_agent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    enabled TINYINT DEFAULT 1,
    UNIQUE KEY uk_channel_agent (channel_id, agent_id)
);

-- 用户绑定表
CREATE TABLE agentx_user_channel_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    channel_type VARCHAR(32) NOT NULL,
    channel_user_id VARCHAR(128) NOT NULL,
    channel_username VARCHAR(128),
    bind_time DATETIME NOT NULL,
    unbind_time DATETIME,
    status TINYINT DEFAULT 1,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    UNIQUE KEY uk_channel_user (tenant_id, channel_type, channel_user_id),
    INDEX idx_user (tenant_id, user_id)
);
```

**验收标准：**
- [x] 表创建成功
- [x] 索引正确
- [x] 有迁移脚本

---

### Task 5: 基础监控

**工作量：** 3 天
**负责人：** 后端开发 + 运维

#### 子任务

5.1 **集成 Prometheus**
- 暴露 metrics 端点
- 收集关键指标

**监控指标：**
- Agent 创建数
- 任务执行数
- 审批通过率
- 平均响应时间
- API 成功率
- OpenFang 调用成功率

**验收标准：**
- [x] Prometheus 可以抓取指标
- [x] 指标数据正确

5.2 **配置 Grafana 仪表盘**
- 创建 AgentX 监控仪表盘
- 显示关键指标图表

**验收标准：**
- [x] 仪表盘可以正常显示
- [x] 图表清晰易懂

---

### Task 6: 测试和文档

**工作量：** 3 天
**负责人：** 测试 + 技术写作

#### 子任务

6.1 **测试**
- 绑定流程端到端测试
- 角色解析单元测试
- 渠道配置集成测试

**验收标准：**
- [x] 核心流程有测试覆盖
- [x] 所有测试通过

6.2 **文档**
- 用户绑定指南
- 渠道配置指南
- 监控指标说明

**验收标准：**
- [x] 文档清晰完整
- [x] 有截图和示例

---

## Phase B 总结

**总工作量：** 20 天（4 周）

**关键里程碑：**
- Week 1: 渠道配置 UI + 数据库表
- Week 2: 用户绑定流程
- Week 3: 角色解析服务 + 监控
- Week 4: 测试 + 文档

**Phase B 完成后：**
- 外部用户可以一键绑定
- 角色解析服务可用
- 基础监控就绪
