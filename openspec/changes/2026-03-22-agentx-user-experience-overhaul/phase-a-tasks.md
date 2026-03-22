# Phase A：降低复杂度

**工期：** 4-6 周
**优先级：** P0
**目标：** 让业务用户能在 3 分钟内创建 Agent，完全隐藏技术细节

---

## 验收标准

- [ ] 业务管理员配置时，页面不出现任何技术 ID/JSON 输入框
- [ ] 新建 Agent 并绑定流程 <= 3 分钟
- [ ] 流程选择支持规则配置（不需要手动输入 workflow_id）
- [ ] 后台自动生成和维护技术映射
- [ ] 数字员工在组织架构中可见

---

## 前置条件

- [ ] 开发环境搭建完成
- [ ] 数据库权限就绪
- [ ] Flowable 环境可访问
- [ ] 设计稿评审通过

---

## 任务清单

### Task 1: Agent 管理中心 UI

**工作量：** 5 天
**负责人：** 前端开发
**依赖：** 无

#### 1.1 创建 Agent 管理页面结构

**工作量：** 1 天

**实现内容：**
1. 创建路由配置
```typescript
// src/router/modules/agentx.ts
{
  path: '/agentx',
  component: Layout,
  name: 'AgentX',
  meta: { title: 'AgentX 管理', icon: 'robot' },
  children: [
    {
      path: 'agents',
      component: () => import('@/views/agentx/agent/index.vue'),
      name: 'AgentList',
      meta: { title: '数字员工' }
    },
    {
      path: 'agents/create',
      component: () => import('@/views/agentx/agent/create.vue'),
      name: 'AgentCreate',
      meta: { title: '创建数字员工' }
    },
    {
      path: 'agents/:id',
      component: () => import('@/views/agentx/agent/detail.vue'),
      name: 'AgentDetail',
      meta: { title: '数字员工详情' }
    }
  ]
}
```

2. 创建 Agent 列表页面
   - 文件：`src/views/agentx/agent/index.vue`
   - 卡片视图布局
   - 搜索框、筛选器、分页器

3. 创建 Agent 详情页面
   - 文件：`src/views/agentx/agent/detail.vue`
   - Tab 布局：基本信息、流程配置、渠道配置、运行监控、审计日志

**验收标准：**
- [ ] 路由配置正确，菜单显示正常
- [ ] 列表页面可以访问，URL: `/agentx/agents`
- [ ] 详情页面可以访问，URL: `/agentx/agents/:id`
- [ ] 页面布局符合设计稿（Figma/蓝湖链接）
- [ ] 响应式布局，支持 1920x1080 和 1366x768 分辨率
- [ ] 无 console 错误
- [ ] 通过 ESLint 检查

**测试用例：**
1. 访问 `/agentx/agents`，页面正常加载
2. 点击"创建数字员工"按钮，跳转到创建页面
3. 在列表中点击某个 Agent，跳转到详情页
4. 浏览器后退按钮正常工作
5. 刷新页面，状态保持

---

#### 1.2 实现 Agent 列表功能

**工作量：** 1 天

**API 接口：**
```typescript
// src/api/agentx/agent/index.ts
export interface AgentVO {
  id: number
  agentName: string
  agentKey: string
  description: string
  avatarUrl: string
  deptId: number
  deptName: string
  status: number  // 1=激活 0=停用
  templateType: string
  createdTime: string
  updatedTime: string
}

export interface AgentPageReqVO {
  pageNo: number
  pageSize: number
  agentName?: string
  deptId?: number
  status?: number
  templateType?: string
}

// 获取 Agent 分页列表
export const getAgentPage = (params: AgentPageReqVO) => {
  return request.get<PageResult<AgentVO>>({ url: '/agentx/agent/page', params })
}

// 删除 Agent
export const deleteAgent = (id: number) => {
  return request.delete({ url: `/agentx/agent/${id}` })
}

// 启用/停用 Agent
export const updateAgentStatus = (id: number, status: number) => {
  return request.put({ url: `/agentx/agent/${id}/status`, data: { status } })
}
```

**实现内容：**
1. 卡片视图展示
   - 每个卡片显示：头像、名称、描述、部门、状态
   - 卡片操作：查看详情、编辑、启用/停用、删除

2. 搜索和筛选
   - 搜索框：按名称搜索
   - 筛选器：部门、状态、模板类型
   - 重置按钮

3. 分页器
   - 每页 12 个（卡片视图）
   - 支持跳转到指定页

**验收标准：**
- [ ] 列表可以正常加载数据
- [ ] 搜索功能正常，输入名称后点击搜索，列表更新
- [ ] 筛选功能正常，选择部门/状态后列表更新
- [ ] 分页功能正常，切换页码后列表更新
- [ ] 启用/停用功能正常，点击后状态更新，卡片样式变化
- [ ] 删除功能正常，弹出确认框，确认后删除成功
- [ ] 空状态显示正常（无数据时显示空状态图）
- [ ] 加载状态显示正常（loading 动画）
- [ ] 错误状态显示正常（API 失败时显示错误提示）

**测试用例：**
1. 列表加载：打开页面，列表显示 12 个 Agent
2. 搜索：输入"请假"，点击搜索，只显示名称包含"请假"的 Agent
3. 筛选：选择"人事部"，列表只显示人事部的 Agent
4. 分页：点击第 2 页，列表显示第 13-24 个 Agent
5. 启用/停用：点击"停用"按钮，Agent 状态变为停用，卡片变灰
6. 删除：点击删除，弹出确认框，点击确认，Agent 被删除
7. 空状态：筛选条件无匹配结果，显示"暂无数据"
8. 错误处理：断网情况下，显示"加载失败，请重试"

**性能要求：**
- [ ] 列表首次加载时间 < 1 秒
- [ ] 搜索/筛选响应时间 < 500ms
- [ ] 分页切换响应时间 < 300ms

---

#### 1.3 实现 5 步向导

**工作量：** 2 天

**组件结构：**
```
src/views/agentx/agent/create.vue
├── components/
│   ├── StepBasicInfo.vue      (步骤 1)
│   ├── StepTemplate.vue        (步骤 2)
│   ├── StepCapability.vue      (步骤 3)
│   ├── StepProcess.vue         (步骤 4)
│   └── StepPreview.vue         (步骤 5)
```

**步骤 1：基本信息**

表单字段：
```typescript
interface BasicInfoForm {
  agentName: string        // 必填，2-50 字符
  description: string      // 必填，10-200 字符
  deptId: number          // 必填，下拉选择
  avatarUrl: string       // 可选，选择预设图标或上传
}
```

验证规则：
- agentName: 必填，2-50 字符，不能包含特殊字符
- description: 必填，10-200 字符
- deptId: 必填，必须是有效的部门 ID

**验收标准：**
- [ ] 表单字段显示正常
- [ ] 部门下拉框可以加载部门树
- [ ] 头像选择器可以选择预设图标（至少 10 个）
- [ ] 头像上传功能正常，支持 jpg/png，最大 2MB
- [ ] 表单验证正常，必填项为空时显示错误提示
- [ ] 点击"下一步"，验证通过后进入步骤 2
- [ ] 点击"取消"，弹出确认框，确认后返回列表

**测试用例：**
1. 必填验证：不填写名称，点击下一步，显示"请输入员工名称"
2. 长度验证：输入 1 个字符，显示"名称长度为 2-50 字符"
3. 部门选择：点击部门下拉框，显示部门树，选择"人事部"
4. 头像选择：点击头像选择器，显示预设图标，选择一个
5. 头像上传：点击上传，选择图片，上传成功，显示预览
6. 下一步：填写完整信息，点击下一步，进入步骤 2

---

**步骤 2：选择职能模板**

模板数据结构：
```typescript
interface AgentTemplate {
  type: string              // 模板类型
  name: string             // 模板名称
  description: string      // 模板说明
  icon: string            // 图标
  capabilities: string[]  // 默认能力
  processes: string[]     // 推荐流程
}
```

预设模板：
1. 请假审批助手
2. 报销审批助手
3. 客户跟进助手
4. 数据分析助手
5. 库存管理助手
6. 客服助手
7. 自定义（高级）

**验收标准：**
- [ ] 显示 7 个模板卡片
- [ ] 每个卡片显示：图标、名称、说明
- [ ] 点击卡片，卡片高亮选中
- [ ] 选中模板后，显示模板详细说明
- [ ] 选择"自定义"，不自动填充能力和流程
- [ ] 点击"上一步"，返回步骤 1，数据保留
- [ ] 点击"下一步"，进入步骤 3，自动填充能力

**测试用例：**
1. 模板显示：进入步骤 2，显示 7 个模板
2. 模板选择：点击"请假审批助手"，卡片高亮
3. 模板说明：选中后，右侧显示详细说明
4. 自动填充：选择模板后进入步骤 3，能力已勾选
5. 上一步：点击上一步，返回步骤 1，名称等信息保留

---

**步骤 3：配置能力权限**

能力分类：
```typescript
interface Capability {
  key: string           // 能力键
  name: string         // 显示名称
  description: string  // 说明
  category: string     // 分类
}

// 能力分类
const categories = [
  { key: 'bpm', name: '审批管理' },
  { key: 'data', name: '数据查询' },
  { key: 'notification', name: '通知能力' },
  { key: 'integration', name: '系统集成' }
]
```

**验收标准：**
- [ ] 能力按分类分组显示
- [ ] 每个能力有复选框、名称、说明
- [ ] 如果从模板进入，默认能力已勾选
- [ ] 可以勾选/取消勾选能力
- [ ] 至少选择 1 个能力才能进入下一步
- [ ] 点击能力名称，显示详细说明（Tooltip）
- [ ] 点击"上一步"，返回步骤 2
- [ ] 点击"下一步"，进入步骤 4

**测试用例：**
1. 默认勾选：从模板进入，默认能力已勾选
2. 勾选能力：勾选"查看待审批任务"
3. 取消勾选：取消勾选"审批报销申请"
4. 必选验证：取消所有勾选，点击下一步，提示"至少选择一个能力"
5. 能力说明：鼠标悬停在能力名称上，显示详细说明

---

#### 1.4 实现流程选择界面

**工作量：** 1 天

**API 接口：**
```typescript
// 获取 Flowable 流程定义列表
export const getProcessDefinitions = () => {
  return request.get<ProcessDefinitionVO[]>({
    url: '/agentx/process/definitions'
  })
}

interface ProcessDefinitionVO {
  key: string
  name: string
  version: number
  description: string
  category: string
  diagramUrl: string  // 流程图 URL
}
```

**实现内容：**
1. 流程列表
   - 从 Flowable 加载流程定义
   - 支持多选
   - 显示流程名称、版本、分类

2. 流程选择策略
   - 单选：规则选择 / AI 自动选择
   - 规则选择：显示规则配置器
   - AI 自动选择：隐藏规则配置器

3. 规则配置器
   - 可视化规则编辑
   - 支持添加/删除规则
   - 规则格式：条件 → 流程

**验收标准：**
- [ ] 可以加载 Flowable 流程定义列表
- [ ] 流程列表显示正常（名称、版本、分类）
- [ ] 可以勾选多个流程
- [ ] 点击"查看流程图"，弹出流程图预览
- [ ] 选择策略切换正常
- [ ] 选择"规则选择"，显示规则配置器
- [ ] 选择"AI 自动选择"，隐藏规则配置器
- [ ] 规则配置器可以添加规则
- [ ] 规则配置器可以删除规则
- [ ] 规则条件支持：用户角色、请假天数、金额等
- [ ] 至少选择 1 个流程才能进入下一步

**测试用例：**
1. 流程加载：进入步骤 4，流程列表显示 5 个流程
2. 流程选择：勾选"请假审批流程（标准）"
3. 流程图预览：点击"查看流程图"，弹出流程图
4. 策略切换：选择"规则选择"，显示规则配置器
5. 添加规则：点击"添加规则"，显示规则表单
6. 配置规则：配置"当用户角色=高管 → 使用请假审批流程（高管版）"
7. 删除规则：点击删除按钮，规则被删除
8. 必选验证：不选择流程，点击下一步，提示"至少选择一个流程"

**规则配置器示例：**
```vue
<template>
  <div class="rule-config">
    <el-button @click="addRule">添加规则</el-button>
    <div v-for="(rule, index) in rules" :key="index" class="rule-item">
      <el-select v-model="rule.field" placeholder="选择字段">
        <el-option label="用户角色" value="userRole" />
        <el-option label="请假天数" value="leaveDays" />
        <el-option label="金额" value="amount" />
      </el-select>
      <el-select v-model="rule.operator" placeholder="选择操作符">
        <el-option label="等于" value="=" />
        <el-option label="大于" value=">" />
        <el-option label="小于" value="<" />
      </el-select>
      <el-input v-model="rule.value" placeholder="输入值" />
      <span>→</span>
      <el-select v-model="rule.processKey" placeholder="选择流程">
        <el-option
          v-for="p in selectedProcesses"
          :key="p.key"
          :label="p.name"
          :value="p.key"
        />
      </el-select>
      <el-button @click="deleteRule(index)" type="danger" icon="Delete" />
    </div>
  </div>
</template>
```

---

**步骤 5：预览与激活**

**验收标准：**
- [ ] 显示完整配置预览
- [ ] 预览内容包括：基本信息、模板、能力、流程、规则
- [ ] 点击"保存草稿"，保存但不激活
- [ ] 点击"激活并发布"，调用后台 API 激活
- [ ] 激活成功，显示成功提示，跳转到详情页
- [ ] 激活失败，显示错误信息，停留在当前页

**测试用例：**
1. 预览显示：进入步骤 5，显示完整配置
2. 保存草稿：点击"保存草稿"，保存成功，返回列表
3. 激活成功：点击"激活并发布"，激活成功，跳转到详情页
4. 激活失败：模拟 API 失败，显示错误信息

---

**整体验收标准：**
- [ ] 5 个步骤可以正常切换
- [ ] 每步数据可以保留（前进后退不丢失）
- [ ] 步骤指示器显示当前步骤
- [ ] 支持保存草稿（任意步骤）
- [ ] 支持取消操作（弹出确认框）
- [ ] 整个流程 < 3 分钟完成
- [ ] 无 console 错误
- [ ] 通过 E2E 测试

**E2E 测试用例：**
```typescript
describe('创建 Agent 流程', () => {
  it('完整流程测试', () => {
    // 1. 进入创建页面
    cy.visit('/agentx/agents/create')

    // 2. 步骤 1：填写基本信息
    cy.get('[data-test="agentName"]').type('测试助手')
    cy.get('[data-test="description"]').type('这是一个测试用的数字员工')
    cy.get('[data-test="deptId"]').click()
    cy.contains('人事部').click()
    cy.get('[data-test="nextBtn"]').click()

    // 3. 步骤 2：选择模板
    cy.contains('请假审批助手').click()
    cy.get('[data-test="nextBtn"]').click()

    // 4. 步骤 3：配置能力
    cy.get('[data-test="capability-bpm_query_tasks"]').should('be.checked')
    cy.get('[data-test="nextBtn"]').click()

    // 5. 步骤 4：选择流程
    cy.contains('请假审批流程（标准）').click()
    cy.get('[data-test="nextBtn"]').click()

    // 6. 步骤 5：激活
    cy.get('[data-test="activateBtn"]').click()
    cy.contains('激活成功')
    cy.url().should('include', '/agentx/agents/')
  })
})
```

---

### Task 2: 数字员工在组织架构中集成

**工作量：** 3 天
**负责人：** 后端开发
**依赖：** 无

#### 2.1 扩展用户表支持数字员工

**工作量：** 1 天

**数据库变更：**
```sql
-- 1. 添加用户类型字段
ALTER TABLE system_users
ADD COLUMN user_type VARCHAR(16) DEFAULT 'human' COMMENT '用户类型：human=真实员工, agent=数字员工';

-- 2. 添加索引
CREATE INDEX idx_user_type ON system_users(user_type);
CREATE INDEX idx_tenant_type ON system_users(tenant_id, user_type);

-- 3. 添加 agent_id 关联字段
ALTER TABLE system_users
ADD COLUMN agent_id BIGINT NULL COMMENT '关联的 Agent ID（仅数字员工）';

CREATE INDEX idx_agent_id ON system_users(agent_id);
```

**后端实现：**
```java
// 1. 扩展 UserDO
@TableName("system_users")
public class UserDO {
    // ... 原有字段

    @TableField("user_type")
    private String userType;  // human / agent

    @TableField("agent_id")
    private Long agentId;
}

// 2. 创建数字员工时自动创建用户记录
@Service
public class AgentServiceImpl implements AgentService {

    @Transactional
    @Override
    public Long createAgent(AgentCreateReqVO reqVO) {
        // 1. 创建 Agent 记录
        AgentDO agent = AgentConvert.INSTANCE.convert(reqVO);
        agentMapper.insert(agent);

        // 2. 创建对应的用户记录
        UserDO user = new UserDO();
        user.setUsername("agent_" + agent.getAgentKey());
        user.setNickname(agent.getAgentName());
        user.setUserType("agent");
        user.setAgentId(agent.getId());
        user.setDeptId(agent.getDeptId());
        user.setStatus(agent.getStatus());
        userMapper.insert(user);

        return agent.getId();
    }
}
```

**验收标准：**
- [ ] 数据库迁移脚本执行成功
- [ ] `system_users` 表增加 `user_type` 和 `agent_id` 字段
- [ ] 索引创建成功
- [ ] 创建 Agent 时自动创建用户记录
- [ ] 用户记录的 `user_type` 为 `agent`
- [ ] 用户记录的 `agent_id` 关联正确
- [ ] 用户名格式为 `agent_{agentKey}`
- [ ] 事务回滚正常（创建失败时用户记录也回滚）

**测试用例：**
```java
@Test
public void testCreateAgent() {
    // 1. 创建 Agent
    AgentCreateReqVO reqVO = new AgentCreateReqVO();
    reqVO.setAgentName("测试助手");
    reqVO.setAgentKey("test_agent");
    reqVO.setDeptId(1L);

    Long agentId = agentService.createAgent(reqVO);

    // 2. 验证 Agent 创建成功
    assertNotNull(agentId);
    AgentDO agent = agentMapper.selectById(agentId);
    assertEquals("测试助手", agent.getAgentName());

    // 3. 验证用户记录创建成功
    UserDO user = userMapper.selectOne(
        new LambdaQueryWrapper<UserDO>()
            .eq(UserDO::getAgentId, agentId)
    );
    assertNotNull(user);
    assertEquals("agent", user.getUserType());
    assertEquals("agent_test_agent", user.getUsername());
    assertEquals(agentId, user.getAgentId());
}

@Test
public void testCreateAgentRollback() {
    // 模拟创建失败，验证事务回滚
    // ...
}
```

---

#### 2.2 组织架构查询 API 支持数字员工

**工作量：** 1 天

**API 接口：**
```java
// 1. 部门成员列表（包含数字员工）
@GetMapping("/system/dept/{id}/members")
public CommonResult<List<DeptMemberVO>> getDeptMembers(
    @PathVariable Long id,
    @RequestParam(required = false) String userType  // human / agent / all
) {
    List<DeptMemberVO> members = deptService.getDeptMembers(id, userType);
    return success(members);
}

// 2. 数字员工列表
@GetMapping("/system/users/agents")
public CommonResult<PageResult<UserVO>> getAgentUsers(
    @Valid UserPageReqVO reqVO
) {
    reqVO.setUserType("agent");
    PageResult<UserDO> pageResult = userService.getUserPage(reqVO);
    return success(UserConvert.INSTANCE.convertPage(pageResult));
}
```

**VO 定义：**
```java
@Data
public class DeptMemberVO {
    private Long id;
    private String username;
    private String nickname;
    private String userType;  // human / agent
    private Long agentId;     // 如果是数字员工
    private String avatar;
    private Integer status;
}
```

**Service 实现：**
```java
@Service
public class DeptServiceImpl implements DeptService {

    @Override
    public List<DeptMemberVO> getDeptMembers(Long deptId, String userType) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getDeptId, deptId);

        if (StringUtils.hasText(userType) && !"all".equals(userType)) {
            wrapper.eq(UserDO::getUserType, userType);
        }

        List<UserDO> users = userMapper.selectList(wrapper);
        return DeptConvert.INSTANCE.convertMembers(users);
    }
}
```

**验收标准：**
- [ ] API 接口实现正确
- [ ] 可以查询部门的所有成员（真实员工 + 数字员工）
- [ ] 可以按 `userType` 筛选
- [ ] 返回数据包含 `userType` 和 `agentId` 字段
- [ ] 数字员工有特殊标识（前端可区分）
- [ ] 单元测试覆盖

**测试用例：**
```java
@Test
public void testGetDeptMembers() {
    // 1. 准备数据：部门 1 有 2 个真实员工 + 1 个数字员工
    // ...

    // 2. 查询所有成员
    List<DeptMemberVO> allMembers = deptService.getDeptMembers(1L, "all");
    assertEquals(3, allMembers.size());

    // 3. 只查询真实员工
    List<DeptMemberVO> humans = deptService.getDeptMembers(1L, "human");
    assertEquals(2, humans.size());

    // 4. 只查询数字员工
    List<DeptMemberVO> agents = deptService.getDeptMembers(1L, "agent");
    assertEquals(1, agents.size());
    assertEquals("agent", agents.get(0).getUserType());
}
```

---

#### 2.3 前端组织架构树显示数字员工

**工作量：** 1 天

**实现内容：**
```vue
<template>
  <el-tree
    :data="deptTree"
    :props="treeProps"
    node-key="id"
  >
    <template #default="{ node, data }">
      <span class="custom-tree-node">
        <!-- 部门节点 -->
        <template v-if="data.type === 'dept'">
          <el-icon><OfficeBuilding /></el-icon>
          <span>{{ data.name }}</span>
        </template>

        <!-- 真实员工节点 -->
        <template v-else-if="data.userType === 'human'">
          <el-avatar :size="20" :src="data.avatar" />
          <span>{{ data.nickname }}</span>
        </template>

        <!-- 数字员工节点 -->
        <template v-else-if="data.userType === 'agent'">
          <el-icon class="agent-icon"><Robot /></el-icon>
          <span>{{ data.nickname }}</span>
          <el-tag size="small" type="info">AI</el-tag>
        </template>
      </span>
    </template>
  </el-tree>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDeptTree } from '@/api/system/dept'

const deptTree = ref([])

const loadDeptTree = async () => {
  const res = await getDeptTree({ includeMembers: true })
  deptTree.value = res.data
}

onMounted(() => {
  loadDeptTree()
})
</script>

<style scoped>
.agent-icon {
  color: #409eff;
}
</style>
```

**验收标准：**
- [ ] 组织架构树可以正常显示
- [ ] 部门节点显示部门图标
- [ ] 真实员工显示头像
- [ ] 数字员工显示机器人图标 + "AI" 标签
- [ ] 数字员工有视觉区分（颜色、图标）
- [ ] 点击数字员工，可以查看详情

**测试用例：**
1. 加载组织架构树，显示所有部门和成员
2. 展开"人事部"，显示 2 个真实员工 + 1 个数字员工
3. 数字员工有机器人图标和"AI"标签
4. 点击数字员工，跳转到 Agent 详情页



---

### Task 3: Agent 与 Flowable 流程关联

**工作量：** 3 天
**负责人：** 后端开发
**依赖：** Task 2 完成

#### 3.1 创建关联表和基础 CRUD

**工作量：** 0.5 天

**数据库表：**
```sql
CREATE TABLE agentx_agent_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    agent_id BIGINT NOT NULL COMMENT 'Agent ID',
    process_definition_key VARCHAR(128) NOT NULL COMMENT 'Flowable 流程定义 key',
    process_name VARCHAR(128) COMMENT '流程名称',
    process_version INT DEFAULT 1 COMMENT '流程版本',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    selection_mode VARCHAR(32) NOT NULL COMMENT '选择模式：rule=规则选择, auto=AI自动选择',
    selection_rules JSON COMMENT '选择规则（JSON 数组）',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：1=启用 0=停用',
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    INDEX idx_agent (tenant_id, agent_id),
    INDEX idx_process (process_definition_key),
    UNIQUE KEY uk_agent_process (tenant_id, agent_id, process_definition_key)
) COMMENT='Agent 与流程关联表';
```

**Entity 定义：**
```java
@TableName("agentx_agent_process")
@Data
public class AgentProcessDO extends BaseDO {
    private Long id;
    private Long tenantId;
    private Long agentId;
    private String processDefinitionKey;
    private String processName;
    private Integer processVersion;
    private Integer priority;
    private String selectionMode;  // rule / auto
    private String selectionRules;  // JSON string
    private Integer enabled;
}
```

**Mapper 接口：**
```java
@Mapper
public interface AgentProcessMapper extends BaseMapperX<AgentProcessDO> {

    default List<AgentProcessDO> selectListByAgentId(Long agentId) {
        return selectList(new LambdaQueryWrapper<AgentProcessDO>()
            .eq(AgentProcessDO::getAgentId, agentId)
            .eq(AgentProcessDO::getEnabled, 1)
            .orderByDesc(AgentProcessDO::getPriority));
    }

    default AgentProcessDO selectByAgentAndProcess(Long agentId, String processKey) {
        return selectOne(new LambdaQueryWrapper<AgentProcessDO>()
            .eq(AgentProcessDO::getAgentId, agentId)
            .eq(AgentProcessDO::getProcessDefinitionKey, processKey));
    }
}
```

**验收标准：**
- [ ] 数据库表创建成功
- [ ] 索引和唯一约束正确
- [ ] Entity 和 Mapper 实现正确
- [ ] 基础 CRUD 方法可用
- [ ] 单元测试通过

**测试用例：**
```java
@Test
public void testAgentProcessCRUD() {
    // 1. 插入
    AgentProcessDO process = new AgentProcessDO();
    process.setAgentId(1L);
    process.setProcessDefinitionKey("leave_approval");
    process.setProcessName("请假审批流程");
    process.setSelectionMode("rule");
    agentProcessMapper.insert(process);

    // 2. 查询
    List<AgentProcessDO> list = agentProcessMapper.selectListByAgentId(1L);
    assertEquals(1, list.size());

    // 3. 更新
    process.setPriority(10);
    agentProcessMapper.updateById(process);

    // 4. 删除
    agentProcessMapper.deleteById(process.getId());
}
```

---

#### 3.2 实现规则选择引擎

**工作量：** 1.5 天

**规则数据结构：**
```java
@Data
public class ProcessSelectionRule {
    private String field;      // 字段名：userRole, leaveDays, amount
    private String operator;   // 操作符：=, >, <, >=, <=, !=, in, contains
    private Object value;      // 值
    private String processKey; // 匹配的流程 key
}
```

**规则引擎实现：**
```java
@Service
public class ProcessSelectionService {

    @Resource
    private AgentProcessMapper agentProcessMapper;

    /**
     * 根据规则选择流程
     */
    public String selectProcessByRule(Long agentId, Map<String, Object> context) {
        // 1. 获取 Agent 关联的所有流程
        List<AgentProcessDO> processes = agentProcessMapper.selectListByAgentId(agentId);

        if (processes.isEmpty()) {
            throw new ServiceException("Agent 未关联任何流程");
        }

        // 2. 如果只有一个流程，直接返回
        if (processes.size() == 1) {
            return processes.get(0).getProcessDefinitionKey();
        }

        // 3. 遍历流程，匹配规则
        for (AgentProcessDO process : processes) {
            if ("rule".equals(process.getSelectionMode())) {
                if (matchRules(process.getSelectionRules(), context)) {
                    return process.getProcessDefinitionKey();
                }
            }
        }

        // 4. 没有匹配的规则，返回优先级最高的
        return processes.get(0).getProcessDefinitionKey();
    }

    /**
     * 匹配规则
     */
    private boolean matchRules(String rulesJson, Map<String, Object> context) {
        if (StringUtils.isEmpty(rulesJson)) {
            return false;
        }

        List<ProcessSelectionRule> rules = JSON.parseArray(rulesJson, ProcessSelectionRule.class);

        // 所有规则都匹配才返回 true（AND 逻辑）
        for (ProcessSelectionRule rule : rules) {
            if (!matchRule(rule, context)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 匹配单个规则
     */
    private boolean matchRule(ProcessSelectionRule rule, Map<String, Object> context) {
        Object contextValue = context.get(rule.getField());
        if (contextValue == null) {
            return false;
        }

        switch (rule.getOperator()) {
            case "=":
                return Objects.equals(contextValue, rule.getValue());
            case ">":
                return compareNumber(contextValue, rule.getValue()) > 0;
            case "<":
                return compareNumber(contextValue, rule.getValue()) < 0;
            case ">=":
                return compareNumber(contextValue, rule.getValue()) >= 0;
            case "<=":
                return compareNumber(contextValue, rule.getValue()) <= 0;
            case "!=":
                return !Objects.equals(contextValue, rule.getValue());
            case "in":
                return isIn(contextValue, rule.getValue());
            case "contains":
                return contains(contextValue, rule.getValue());
            default:
                return false;
        }
    }

    private int compareNumber(Object v1, Object v2) {
        BigDecimal bd1 = new BigDecimal(v1.toString());
        BigDecimal bd2 = new BigDecimal(v2.toString());
        return bd1.compareTo(bd2);
    }

    private boolean isIn(Object value, Object list) {
        if (list instanceof Collection) {
            return ((Collection<?>) list).contains(value);
        }
        return false;
    }

    private boolean contains(Object value, Object substring) {
        return value.toString().contains(substring.toString());
    }
}
```

**验收标准：**
- [ ] 规则引擎实现正确
- [ ] 支持所有操作符（=, >, <, >=, <=, !=, in, contains）
- [ ] 支持数字比较
- [ ] 支持字符串匹配
- [ ] 支持集合判断
- [ ] 多条规则 AND 逻辑正确
- [ ] 单元测试覆盖率 > 80%

**测试用例：**
```java
@Test
public void testRuleSelection() {
    // 准备数据
    AgentProcessDO process1 = new AgentProcessDO();
    process1.setProcessDefinitionKey("leave_approval_standard");
    process1.setSelectionMode("rule");
    process1.setSelectionRules("[{\"field\":\"leaveDays\",\"operator\":\"<=\",\"value\":2}]");

    AgentProcessDO process2 = new AgentProcessDO();
    process2.setProcessDefinitionKey("leave_approval_manager");
    process2.setSelectionMode("rule");
    process2.setSelectionRules("[{\"field\":\"leaveDays\",\"operator\":\">\",\"value\":2}]");

    // 测试场景 1：请假 1 天
    Map<String, Object> context1 = Map.of("leaveDays", 1);
    String result1 = processSelectionService.selectProcessByRule(1L, context1);
    assertEquals("leave_approval_standard", result1);

    // 测试场景 2：请假 3 天
    Map<String, Object> context2 = Map.of("leaveDays", 3);
    String result2 = processSelectionService.selectProcessByRule(1L, context2);
    assertEquals("leave_approval_manager", result2);
}

@Test
public void testComplexRule() {
    // 复杂规则：用户角色=高管 AND 请假天数<=5
    String rules = "[" +
        "{\"field\":\"userRole\",\"operator\":\"=\",\"value\":\"executive\"}," +
        "{\"field\":\"leaveDays\",\"operator\":\"<=\",\"value\":5}" +
        "]";

    Map<String, Object> context = Map.of(
        "userRole", "executive",
        "leaveDays", 3
    );

    boolean match = processSelectionService.matchRules(rules, context);
    assertTrue(match);
}
```

---

#### 3.3 实现 AI 自动选择

**工作量：** 1 天

**实现：**
```java
@Service
public class ProcessSelectionService {

    @Resource
    private LLMService llmService;

    /**
     * AI 自动选择流程
     */
    public String selectProcessByAI(Long agentId, Map<String, Object> context) {
        // 1. 获取所有可用流程
        List<AgentProcessDO> processes = agentProcessMapper.selectListByAgentId(agentId);

        if (processes.isEmpty()) {
            throw new ServiceException("Agent 未关联任何流程");
        }

        if (processes.size() == 1) {
            return processes.get(0).getProcessDefinitionKey();
        }

        // 2. 构建 Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个流程选择助手，需要根据任务上下文选择最合适的流程。\n\n");
        prompt.append("可用的流程：\n");

        for (int i = 0; i < processes.size(); i++) {
            AgentProcessDO process = processes.get(i);
            prompt.append(String.format("%d. %s (key: %s)\n",
                i + 1, process.getProcessName(), process.getProcessDefinitionKey()));

            // 如果有规则描述，也加入 Prompt
            if (StringUtils.hasText(process.getSelectionRules())) {
                prompt.append("   适用场景：").append(process.getSelectionRules()).append("\n");
            }
        }

        prompt.append("\n当前任务上下文：\n");
        prompt.append(JSON.toJSONString(context, true));
        prompt.append("\n\n请选择最合适的流程，只返回流程的 key，不要返回其他内容。");

        // 3. 调用 LLM
        String response = llmService.chat(prompt.toString());

        // 4. 解析响应
        String selectedKey = response.trim();

        // 5. 验证返回的 key 是否有效
        boolean valid = processes.stream()
            .anyMatch(p -> p.getProcessDefinitionKey().equals(selectedKey));

        if (!valid) {
            log.warn("LLM 返回的流程 key 无效: {}, 使用默认流程", selectedKey);
            return processes.get(0).getProcessDefinitionKey();
        }

        return selectedKey;
    }
}
```

**验收标准：**
- [ ] AI 选择功能实现
- [ ] Prompt 构建正确
- [ ] LLM 响应解析正确
- [ ] 返回无效 key 时有降级处理
- [ ] 有日志记录
- [ ] 有超时控制（30s）
- [ ] 集成测试通过

**测试用例：**
```java
@Test
public void testAISelection() {
    // Mock LLM 服务
    when(llmService.chat(anyString())).thenReturn("leave_approval_manager");

    Map<String, Object> context = Map.of(
        "userRole", "employee",
        "leaveDays", 3,
        "leaveType", "annual"
    );

    String result = processSelectionService.selectProcessByAI(1L, context);
    assertEquals("leave_approval_manager", result);

    // 验证 Prompt 包含必要信息
    ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
    verify(llmService).chat(promptCaptor.capture());
    String prompt = promptCaptor.getValue();
    assertTrue(prompt.contains("可用的流程"));
    assertTrue(prompt.contains("当前任务上下文"));
}
```



---

### Task 4: 流程模板库

**工作量：** 3 天
**负责人：** 产品 + 后端开发
**依赖：** 无

#### 4.1 设计 6 个常用模板

**工作量：** 1 天
**负责人：** 产品经理

**模板清单：**

1. **请假审批助手**
```json
{
  "type": "leave_approval",
  "name": "请假审批助手",
  "description": "自动处理员工请假申请，根据天数自动审批或提交人工审批",
  "icon": "calendar",
  "capabilities": [
    "bpm_query_tasks",
    "bpm_approve",
    "notification_send",
    "user_query"
  ],
  "recommendedProcesses": [
    "leave_approval_standard",
    "leave_approval_executive"
  ],
  "defaultRules": [
    {
      "field": "leaveDays",
      "operator": "<=",
      "value": 2,
      "processKey": "leave_approval_standard"
    }
  ]
}
```

2. **报销审批助手**
```json
{
  "type": "expense_approval",
  "name": "报销审批助手",
  "description": "自动处理报销申请，验证发票并根据金额自动审批",
  "icon": "money",
  "capabilities": [
    "bpm_query_tasks",
    "bpm_approve",
    "notification_send",
    "invoice_verify",
    "finance_query"
  ],
  "recommendedProcesses": [
    "expense_approval_standard"
  ],
  "defaultRules": [
    {
      "field": "amount",
      "operator": "<",
      "value": 500,
      "processKey": "expense_approval_standard"
    }
  ]
}
```

3. **客户跟进助手**
```json
{
  "type": "customer_followup",
  "name": "客户跟进助手",
  "description": "自动跟进客户，提醒销售人员及时联系",
  "icon": "user",
  "capabilities": [
    "crm_query",
    "task_create",
    "notification_send",
    "email_send"
  ],
  "recommendedProcesses": [
    "customer_followup_standard"
  ]
}
```

4. **数据分析助手**
```json
{
  "type": "data_analysis",
  "name": "数据分析助手",
  "description": "定期生成数据报表，分析业务趋势",
  "icon": "chart",
  "capabilities": [
    "data_query",
    "report_generate",
    "notification_send",
    "file_upload"
  ],
  "recommendedProcesses": [
    "report_generation"
  ]
}
```

5. **库存管理助手**
```json
{
  "type": "inventory_management",
  "name": "库存管理助手",
  "description": "监控库存水平，自动生成采购建议",
  "icon": "box",
  "capabilities": [
    "inventory_query",
    "purchase_create",
    "notification_send",
    "supplier_query"
  ],
  "recommendedProcesses": [
    "inventory_check",
    "purchase_request"
  ]
}
```

6. **客服助手**
```json
{
  "type": "customer_service",
  "name": "客服助手",
  "description": "处理客户投诉和咨询，自动分类和分配",
  "icon": "service",
  "capabilities": [
    "ticket_query",
    "ticket_assign",
    "notification_send",
    "knowledge_search"
  ],
  "recommendedProcesses": [
    "complaint_handling",
    "inquiry_handling"
  ]
}
```

**验收标准：**
- [ ] 6 个模板设计完成
- [ ] 每个模板有清晰的名称和描述
- [ ] 每个模板有合适的图标
- [ ] 每个模板有默认的能力配置
- [ ] 每个模板有推荐的流程
- [ ] 模板 JSON 格式正确
- [ ] 产品评审通过

---

#### 4.2 实现模板存储和加载

**工作量：** 1 天
**负责人：** 后端开发

**存储方式：**
```
resources/templates/agent/
├── leave_approval.json
├── expense_approval.json
├── customer_followup.json
├── data_analysis.json
├── inventory_management.json
└── customer_service.json
```

**模板加载服务：**
```java
@Service
public class AgentTemplateService {

    private static final String TEMPLATE_PATH = "templates/agent/";
    private Map<String, AgentTemplate> templateCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadTemplates();
    }

    /**
     * 加载所有模板
     */
    private void loadTemplates() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:" + TEMPLATE_PATH + "*.json");

            for (Resource resource : resources) {
                String json = StreamUtils.copyToString(
                    resource.getInputStream(), StandardCharsets.UTF_8);
                AgentTemplate template = JSON.parseObject(json, AgentTemplate.class);
                templateCache.put(template.getType(), template);
            }

            log.info("加载 Agent 模板成功，共 {} 个", templateCache.size());
        } catch (Exception e) {
            log.error("加载 Agent 模板失败", e);
        }
    }

    /**
     * 获取所有模板
     */
    public List<AgentTemplate> getAllTemplates() {
        return new ArrayList<>(templateCache.values());
    }

    /**
     * 根据类型获取模板
     */
    public AgentTemplate getTemplate(String type) {
        return templateCache.get(type);
    }

    /**
     * 应用模板到 Agent 配置
     */
    public void applyTemplate(AgentCreateReqVO reqVO, String templateType) {
        AgentTemplate template = getTemplate(templateType);
        if (template == null) {
            throw new ServiceException("模板不存在: " + templateType);
        }

        // 设置默认能力
        reqVO.setCapabilities(template.getCapabilities());

        // 设置推荐流程
        reqVO.setRecommendedProcesses(template.getRecommendedProcesses());

        // 设置默认规则
        if (CollectionUtils.isNotEmpty(template.getDefaultRules())) {
            reqVO.setSelectionRules(JSON.toJSONString(template.getDefaultRules()));
        }
    }
}
```

**API 接口：**
```java
@RestController
@RequestMapping("/agentx/template")
public class AgentTemplateController {

    @Resource
    private AgentTemplateService templateService;

    /**
     * 获取所有模板
     */
    @GetMapping("/list")
    public CommonResult<List<AgentTemplate>> getTemplateList() {
        List<AgentTemplate> templates = templateService.getAllTemplates();
        return success(templates);
    }

    /**
     * 获取单个模板
     */
    @GetMapping("/{type}")
    public CommonResult<AgentTemplate> getTemplate(@PathVariable String type) {
        AgentTemplate template = templateService.getTemplate(type);
        if (template == null) {
            return error("模板不存在");
        }
        return success(template);
    }
}
```

**验收标准：**
- [ ] 模板文件存储在正确位置
- [ ] 服务启动时自动加载模板
- [ ] 可以获取所有模板列表
- [ ] 可以根据类型获取单个模板
- [ ] 应用模板功能正常
- [ ] 模板缓存正常工作
- [ ] 单元测试通过

**测试用例：**
```java
@Test
public void testLoadTemplates() {
    List<AgentTemplate> templates = templateService.getAllTemplates();
    assertEquals(6, templates.size());
}

@Test
public void testGetTemplate() {
    AgentTemplate template = templateService.getTemplate("leave_approval");
    assertNotNull(template);
    assertEquals("请假审批助手", template.getName());
    assertTrue(template.getCapabilities().contains("bpm_query_tasks"));
}

@Test
public void testApplyTemplate() {
    AgentCreateReqVO reqVO = new AgentCreateReqVO();
    templateService.applyTemplate(reqVO, "leave_approval");

    assertNotNull(reqVO.getCapabilities());
    assertTrue(reqVO.getCapabilities().contains("bpm_approve"));
    assertNotNull(reqVO.getRecommendedProcesses());
}
```

---

#### 4.3 前端模板选择器

**工作量：** 1 天
**负责人：** 前端开发

**组件实现：**
```vue
<template>
  <div class="template-selector">
    <el-row :gutter="20">
      <el-col
        v-for="template in templates"
        :key="template.type"
        :span="8"
      >
        <el-card
          :class="['template-card', { selected: selectedType === template.type }]"
          @click="selectTemplate(template)"
        >
          <div class="template-icon">
            <el-icon :size="48">
              <component :is="getIcon(template.icon)" />
            </el-icon>
          </div>
          <h3>{{ template.name }}</h3>
          <p class="description">{{ template.description }}</p>
          <el-tag v-if="selectedType === template.type" type="success">
            已选择
          </el-tag>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模板详情 -->
    <el-card v-if="selectedTemplate" class="template-detail">
      <h4>模板详情</h4>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">
          {{ selectedTemplate.name }}
        </el-descriptions-item>
        <el-descriptions-item label="说明">
          {{ selectedTemplate.description }}
        </el-descriptions-item>
        <el-descriptions-item label="默认能力">
          <el-tag
            v-for="cap in selectedTemplate.capabilities"
            :key="cap"
            size="small"
            style="margin-right: 8px"
          >
            {{ getCapabilityName(cap) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="推荐流程">
          <div v-for="proc in selectedTemplate.recommendedProcesses" :key="proc">
            {{ proc }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTemplateList } from '@/api/agentx/template'
import type { AgentTemplate } from '@/api/agentx/types'

const templates = ref<AgentTemplate[]>([])
const selectedType = ref<string>('')
const selectedTemplate = ref<AgentTemplate | null>(null)

const emit = defineEmits(['select'])

const loadTemplates = async () => {
  const res = await getTemplateList()
  templates.value = res.data
}

const selectTemplate = (template: AgentTemplate) => {
  selectedType.value = template.type
  selectedTemplate.value = template
  emit('select', template)
}

const getIcon = (iconName: string) => {
  const iconMap = {
    calendar: 'Calendar',
    money: 'Money',
    user: 'User',
    chart: 'DataAnalysis',
    box: 'Box',
    service: 'Service'
  }
  return iconMap[iconName] || 'Document'
}

const getCapabilityName = (key: string) => {
  const nameMap = {
    bpm_query_tasks: '查询待办',
    bpm_approve: '审批',
    notification_send: '发送通知',
    user_query: '查询用户',
    invoice_verify: '发票验证',
    // ... 更多映射
  }
  return nameMap[key] || key
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.template-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.template-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.template-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.template-icon {
  margin-bottom: 16px;
  color: #409eff;
}

.description {
  color: #666;
  font-size: 14px;
  min-height: 40px;
}

.template-detail {
  margin-top: 20px;
}
</style>
```

**验收标准：**
- [ ] 模板卡片显示正常（6 个）
- [ ] 点击卡片可以选中
- [ ] 选中的卡片有高亮效果
- [ ] 选中后显示模板详情
- [ ] 模板详情显示完整信息
- [ ] 图标显示正确
- [ ] 响应式布局正常
- [ ] 组件可以正确触发 select 事件

**测试用例：**
```typescript
describe('TemplateSelector', () => {
  it('加载模板列表', async () => {
    const wrapper = mount(TemplateSelector)
    await nextTick()
    expect(wrapper.findAll('.template-card')).toHaveLength(6)
  })

  it('选择模板', async () => {
    const wrapper = mount(TemplateSelector)
    await nextTick()

    const firstCard = wrapper.find('.template-card')
    await firstCard.trigger('click')

    expect(wrapper.find('.template-card.selected')).toBeTruthy()
    expect(wrapper.find('.template-detail')).toBeTruthy()
  })

  it('触发 select 事件', async () => {
    const wrapper = mount(TemplateSelector)
    await nextTick()

    const firstCard = wrapper.find('.template-card')
    await firstCard.trigger('click')

    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')[0][0].type).toBe('leave_approval')
  })
})
```



---

### Task 5: 后台自动化处理

**工作量：** 3 天
**负责人：** 后端开发
**依赖：** Task 2, Task 3, Task 4

#### 5.1 实现 Agent 激活逻辑

**工作量：** 1.5 天

**Service 实现：**
```java
@Service
public class AgentServiceImpl implements AgentService {

    @Resource
    private AgentMapper agentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AgentProcessMapper agentProcessMapper;
    @Resource
    private AgentCapabilityMapper agentCapabilityMapper;
    @Resource
    private OpenFangClient openFangClient;
    @Resource
    private AgentTemplateService templateService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createAndActivateAgent(AgentCreateReqVO reqVO) {
        // 1. 验证输入
        validateAgentCreate(reqVO);

        // 2. 创建 Agent 记录
        AgentDO agent = buildAgent(reqVO);
        agentMapper.insert(agent);
        Long agentId = agent.getId();

        try {
            // 3. 创建数字员工用户记录
            createAgentUser(agent);

            // 4. 创建流程关联
            createProcessAssociations(agentId, reqVO);

            // 5. 创建能力配置
            createCapabilities(agentId, reqVO);

            // 6. 如果需要 OpenFang，注册到 OpenFang
            if (needsOpenFang(reqVO)) {
                registerToOpenFang(agent, reqVO);
            }

            // 7. 记录审计日志
            auditLog(agentId, "CREATE_AGENT", reqVO);

            return agentId;

        } catch (Exception e) {
            log.error("激活 Agent 失败: agentId={}", agentId, e);
            throw new ServiceException("激活失败: " + e.getMessage());
        }
    }

    private void validateAgentCreate(AgentCreateReqVO reqVO) {
        // 验证名称唯一性
        if (agentMapper.selectByName(reqVO.getAgentName()) != null) {
            throw new ServiceException("Agent 名称已存在");
        }

        // 验证部门存在
        if (deptMapper.selectById(reqVO.getDeptId()) == null) {
            throw new ServiceException("部门不存在");
        }

        // 验证至少选择一个能力
        if (CollectionUtils.isEmpty(reqVO.getCapabilities())) {
            throw new ServiceException("至少选择一个能力");
        }

        // 验证至少选择一个流程
        if (CollectionUtils.isEmpty(reqVO.getProcessKeys())) {
            throw new ServiceException("至少选择一个流程");
        }
    }

    private AgentDO buildAgent(AgentCreateReqVO reqVO) {
        AgentDO agent = new AgentDO();
        agent.setAgentName(reqVO.getAgentName());
        agent.setAgentKey(generateAgentKey(reqVO.getAgentName()));
        agent.setDescription(reqVO.getDescription());
        agent.setAvatarUrl(reqVO.getAvatarUrl());
        agent.setDeptId(reqVO.getDeptId());
        agent.setTemplateType(reqVO.getTemplateType());
        agent.setStatus(1); // 激活状态
        return agent;
    }

    private String generateAgentKey(String name) {
        // 生成唯一的 agent_key
        String pinyin = PinyinUtil.toPinyin(name, "");
        String key = pinyin.toLowerCase() + "_" + System.currentTimeMillis();
        return key;
    }

    private void createAgentUser(AgentDO agent) {
        UserDO user = new UserDO();
        user.setUsername("agent_" + agent.getAgentKey());
        user.setNickname(agent.getAgentName());
        user.setUserType("agent");
        user.setAgentId(agent.getId());
        user.setDeptId(agent.getDeptId());
        user.setStatus(agent.getStatus());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userMapper.insert(user);
    }

    private void createProcessAssociations(Long agentId, AgentCreateReqVO reqVO) {
        for (String processKey : reqVO.getProcessKeys()) {
            AgentProcessDO process = new AgentProcessDO();
            process.setAgentId(agentId);
            process.setProcessDefinitionKey(processKey);
            process.setProcessName(getProcessName(processKey));
            process.setPriority(0);
            process.setSelectionMode(reqVO.getSelectionMode());
            process.setSelectionRules(reqVO.getSelectionRules());
            process.setEnabled(1);
            agentProcessMapper.insert(process);
        }
    }

    private void createCapabilities(Long agentId, AgentCreateReqVO reqVO) {
        for (String capKey : reqVO.getCapabilities()) {
            AgentCapabilityDO capability = new AgentCapabilityDO();
            capability.setAgentId(agentId);
            capability.setCapabilityKey(capKey);
            capability.setCapabilityName(getCapabilityName(capKey));
            capability.setEnabled(1);
            agentCapabilityMapper.insert(capability);
        }
    }

    private boolean needsOpenFang(AgentCreateReqVO reqVO) {
        // 判断是否需要 OpenFang
        // 如果有复杂 AI 能力，需要 OpenFang
        List<String> complexCapabilities = Arrays.asList(
            "ai_decision", "multi_agent_collaboration", "long_running_task"
        );
        return reqVO.getCapabilities().stream()
            .anyMatch(complexCapabilities::contains);
    }

    private void registerToOpenFang(AgentDO agent, AgentCreateReqVO reqVO) {
        try {
            OpenFangAgentRegisterReqDTO dto = new OpenFangAgentRegisterReqDTO();
            dto.setAgentId(agent.getId().toString());
            dto.setAgentName(agent.getAgentName());
            dto.setCapabilities(reqVO.getCapabilities());
            dto.setCallbackUrl(getCallbackUrl());

            openFangClient.registerAgent(dto);
            log.info("注册 Agent 到 OpenFang 成功: agentId={}", agent.getId());

        } catch (Exception e) {
            log.error("注册 Agent 到 OpenFang 失败: agentId={}", agent.getId(), e);
            throw new ServiceException("注册到 OpenFang 失败: " + e.getMessage());
        }
    }
}
```

**验收标准：**
- [ ] 一键激活完成所有配置
- [ ] 事务正确，失败时全部回滚
- [ ] 输入验证完整
- [ ] Agent 记录创建成功
- [ ] 用户记录创建成功
- [ ] 流程关联创建成功
- [ ] 能力配置创建成功
- [ ] OpenFang 注册成功（如需要）
- [ ] 审计日志记录完整
- [ ] 错误信息清晰
- [ ] 单元测试覆盖率 > 80%

**测试用例：**
```java
@Test
public void testCreateAndActivateAgent() {
    // 准备数据
    AgentCreateReqVO reqVO = new AgentCreateReqVO();
    reqVO.setAgentName("测试助手");
    reqVO.setDescription("测试描述");
    reqVO.setDeptId(1L);
    reqVO.setTemplateType("leave_approval");
    reqVO.setCapabilities(Arrays.asList("bpm_query_tasks", "bpm_approve"));
    reqVO.setProcessKeys(Arrays.asList("leave_approval_standard"));
    reqVO.setSelectionMode("rule");

    // 执行
    Long agentId = agentService.createAndActivateAgent(reqVO);

    // 验证 Agent
    assertNotNull(agentId);
    AgentDO agent = agentMapper.selectById(agentId);
    assertEquals("测试助手", agent.getAgentName());
    assertEquals(1, agent.getStatus());

    // 验证用户
    UserDO user = userMapper.selectOne(
        new LambdaQueryWrapper<UserDO>().eq(UserDO::getAgentId, agentId)
    );
    assertNotNull(user);
    assertEquals("agent", user.getUserType());

    // 验证流程关联
    List<AgentProcessDO> processes = agentProcessMapper.selectListByAgentId(agentId);
    assertEquals(1, processes.size());

    // 验证能力
    List<AgentCapabilityDO> capabilities = agentCapabilityMapper
        .selectList(new LambdaQueryWrapper<AgentCapabilityDO>()
            .eq(AgentCapabilityDO::getAgentId, agentId));
    assertEquals(2, capabilities.size());
}

@Test
public void testCreateAgentRollback() {
    // Mock OpenFang 注册失败
    when(openFangClient.registerAgent(any())).thenThrow(new RuntimeException("网络错误"));

    AgentCreateReqVO reqVO = buildReqVO();
    reqVO.setCapabilities(Arrays.asList("ai_decision")); // 需要 OpenFang

    // 执行并验证异常
    assertThrows(ServiceException.class, () -> {
        agentService.createAndActivateAgent(reqVO);
    });

    // 验证回滚：数据库中不应该有记录
    List<AgentDO> agents = agentMapper.selectList(null);
    assertEquals(0, agents.size());
}
```

---

#### 5.2 实现配置同步

**工作量：** 1 天

**同步服务：**
```java
@Service
public class AgentSyncService {

    @Resource
    private AgentMapper agentMapper;
    @Resource
    private OpenFangClient openFangClient;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 同步 Agent 配置到 OpenFang
     */
    @Async
    public void syncAgentConfig(Long agentId) {
        try {
            // 1. 获取 Agent 完整配置
            AgentDO agent = agentMapper.selectById(agentId);
            if (agent == null) {
                log.warn("Agent 不存在: {}", agentId);
                return;
            }

            // 2. 构建同步数据
            OpenFangAgentConfigDTO config = buildSyncConfig(agent);

            // 3. 调用 OpenFang API
            openFangClient.updateAgentConfig(agentId.toString(), config);

            // 4. 更新同步状态
            updateSyncStatus(agentId, true, null);

            log.info("同步 Agent 配置成功: agentId={}", agentId);

        } catch (Exception e) {
            log.error("同步 Agent 配置失败: agentId={}", agentId, e);
            updateSyncStatus(agentId, false, e.getMessage());

            // 重试
            retrySync(agentId);
        }
    }

    private void retrySync(Long agentId) {
        String key = "agent:sync:retry:" + agentId;
        String retryCount = redisTemplate.opsForValue().get(key);

        int count = retryCount == null ? 0 : Integer.parseInt(retryCount);
        if (count < 3) {
            // 指数退避重试
            long delay = (long) Math.pow(2, count) * 1000; // 1s, 2s, 4s
            CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
                .execute(() -> syncAgentConfig(agentId));

            redisTemplate.opsForValue().set(key, String.valueOf(count + 1), 1, TimeUnit.HOURS);
        } else {
            log.error("同步 Agent 配置失败，已达最大重试次数: agentId={}", agentId);
        }
    }

    private void updateSyncStatus(Long agentId, boolean success, String errorMsg) {
        AgentDO agent = new AgentDO();
        agent.setId(agentId);
        agent.setLastSyncTime(LocalDateTime.now());
        agent.setSyncStatus(success ? 1 : 0);
        agent.setSyncErrorMsg(errorMsg);
        agentMapper.updateById(agent);
    }
}
```

**验收标准：**
- [ ] 配置变更自动触发同步
- [ ] 同步失败有重试机制（最多 3 次）
- [ ] 重试使用指数退避
- [ ] 同步状态记录正确
- [ ] 异步执行不阻塞主流程
- [ ] 有日志记录
- [ ] 单元测试通过

**测试用例：**
```java
@Test
public void testSyncAgentConfig() {
    // Mock OpenFang 调用成功
    when(openFangClient.updateAgentConfig(anyString(), any())).thenReturn(true);

    // 执行同步
    agentSyncService.syncAgentConfig(1L);

    // 等待异步完成
    await().atMost(2, TimeUnit.SECONDS).until(() -> {
        AgentDO agent = agentMapper.selectById(1L);
        return agent.getSyncStatus() == 1;
    });

    // 验证
    verify(openFangClient, times(1)).updateAgentConfig(anyString(), any());
}

@Test
public void testSyncRetry() {
    // Mock 前 2 次失败，第 3 次成功
    when(openFangClient.updateAgentConfig(anyString(), any()))
        .thenThrow(new RuntimeException("网络错误"))
        .thenThrow(new RuntimeException("网络错误"))
        .thenReturn(true);

    // 执行同步
    agentSyncService.syncAgentConfig(1L);

    // 等待重试完成
    await().atMost(10, TimeUnit.SECONDS).until(() -> {
        AgentDO agent = agentMapper.selectById(1L);
        return agent.getSyncStatus() == 1;
    });

    // 验证重试了 3 次
    verify(openFangClient, times(3)).updateAgentConfig(anyString(), any());
}
```

---

#### 5.3 实现配置版本管理

**工作量：** 0.5 天

**版本表：**
```sql
CREATE TABLE agentx_agent_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_id BIGINT NOT NULL,
    version INT NOT NULL,
    config_snapshot JSON NOT NULL COMMENT '配置快照',
    change_description VARCHAR(500) COMMENT '变更说明',
    created_by BIGINT,
    created_time DATETIME NOT NULL,
    INDEX idx_agent (agent_id, version)
) COMMENT='Agent 配置版本表';
```

**版本服务：**
```java
@Service
public class AgentVersionService {

    @Resource
    private AgentVersionMapper versionMapper;

    /**
     * 保存配置版本
     */
    public void saveVersion(Long agentId, AgentConfigSnapshot snapshot, String description) {
        // 获取当前最大版本号
        Integer maxVersion = versionMapper.selectMaxVersion(agentId);
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        // 保存版本
        AgentVersionDO version = new AgentVersionDO();
        version.setAgentId(agentId);
        version.setVersion(newVersion);
        version.setConfigSnapshot(JSON.toJSONString(snapshot));
        version.setChangeDescription(description);
        versionMapper.insert(version);
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public void rollbackToVersion(Long agentId, Integer targetVersion) {
        // 获取目标版本配置
        AgentVersionDO version = versionMapper.selectByAgentAndVersion(agentId, targetVersion);
        if (version == null) {
            throw new ServiceException("版本不存在");
        }

        // 恢复配置
        AgentConfigSnapshot snapshot = JSON.parseObject(
            version.getConfigSnapshot(), AgentConfigSnapshot.class);
        applySnapshot(agentId, snapshot);

        // 保存回滚记录
        saveVersion(agentId, snapshot, "回滚到版本 " + targetVersion);
    }
}
```

**验收标准：**
- [ ] 配置变更自动保存版本
- [ ] 可以查看历史版本
- [ ] 可以回滚到指定版本
- [ ] 版本号自动递增
- [ ] 有变更说明
- [ ] 单元测试通过



---

### Task 6: 数据库表创建

**工作量：** 1 天
**负责人：** 后端开发
**依赖：** 无

#### 6.1 创建所有核心表

**数据库迁移脚本：**
```sql
-- V1.0__create_agentx_tables.sql

-- 1. Agent 基本信息表
CREATE TABLE agentx_agent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    agent_name VARCHAR(128) NOT NULL,
    agent_key VARCHAR(64) NOT NULL,
    description TEXT,
    avatar_url VARCHAR(255),
    dept_id BIGINT,
    template_type VARCHAR(32),
    status TINYINT DEFAULT 1,
    last_sync_time DATETIME,
    sync_status TINYINT,
    sync_error_msg TEXT,
    created_by BIGINT,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    UNIQUE KEY uk_tenant_key (tenant_id, agent_key),
    INDEX idx_dept (dept_id),
    INDEX idx_status (status)
) COMMENT='Agent 基本信息表';

-- 2. Agent 与流程关联表
CREATE TABLE agentx_agent_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    process_definition_key VARCHAR(128) NOT NULL,
    process_name VARCHAR(128),
    process_version INT DEFAULT 1,
    priority INT DEFAULT 0,
    selection_mode VARCHAR(32) NOT NULL,
    selection_rules JSON,
    enabled TINYINT DEFAULT 1,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    INDEX idx_agent (tenant_id, agent_id),
    UNIQUE KEY uk_agent_process (tenant_id, agent_id, process_definition_key)
) COMMENT='Agent 与流程关联表';

-- 3. Agent 能力配置表
CREATE TABLE agentx_agent_capability (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    capability_key VARCHAR(64) NOT NULL,
    capability_name VARCHAR(128),
    enabled TINYINT DEFAULT 1,
    max_calls_per_hour INT,
    conditions JSON,
    created_time DATETIME NOT NULL,
    INDEX idx_agent (tenant_id, agent_id)
) COMMENT='Agent 能力配置表';

-- 4. 任务执行记录表
CREATE TABLE agentx_task_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    task_type VARCHAR(64),
    process_instance_id VARCHAR(64),
    status VARCHAR(32),
    start_time DATETIME,
    end_time DATETIME,
    duration_ms INT,
    error_message TEXT,
    created_time DATETIME NOT NULL,
    INDEX idx_agent_time (tenant_id, agent_id, start_time)
) COMMENT='任务执行记录表';

-- 5. 审计日志表
CREATE TABLE agentx_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    agent_id BIGINT,
    user_id BIGINT,
    action VARCHAR(64),
    resource_type VARCHAR(32),
    resource_id VARCHAR(64),
    details JSON,
    ip_address VARCHAR(64),
    created_time DATETIME NOT NULL,
    INDEX idx_tenant_time (tenant_id, created_time),
    INDEX idx_agent (agent_id)
) COMMENT='审计日志表';

-- 6. Agent 配置版本表
CREATE TABLE agentx_agent_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_id BIGINT NOT NULL,
    version INT NOT NULL,
    config_snapshot JSON NOT NULL,
    change_description VARCHAR(500),
    created_by BIGINT,
    created_time DATETIME NOT NULL,
    INDEX idx_agent (agent_id, version)
) COMMENT='Agent 配置版本表';
```

**验收标准：**
- [ ] 所有表创建成功
- [ ] 索引创建正确
- [ ] 唯一约束正确
- [ ] 注释完整
- [ ] Flyway 迁移脚本可执行
- [ ] 支持多环境（dev/test/prod）
- [ ] 有回滚脚本

---

### Task 7: 单元测试和集成测试

**工作量：** 5 天
**负责人：** 后端开发 + 测试
**依赖：** Task 1-6

#### 7.1 单元测试（3 天）

**测试覆盖：**
- AgentService: 创建、更新、删除、查询
- ProcessSelectionService: 规则匹配、AI 选择
- AgentSyncService: 配置同步、重试
- AgentTemplateService: 模板加载、应用

**目标覆盖率：** > 70%

**验收标准：**
- [ ] 所有 Service 有单元测试
- [ ] 测试覆盖率 > 70%
- [ ] 所有测试通过
- [ ] 测试可自动化运行

#### 7.2 集成测试（2 天）

**测试场景：**
1. 完整的 Agent 创建流程
2. 流程选择和执行
3. 配置同步
4. 版本回滚

**验收标准：**
- [ ] 核心流程有集成测试
- [ ] 测试数据自动准备和清理
- [ ] 所有测试通过



---

### Task 8: 文档编写

**工作量：** 2 天
**负责人：** 技术写作
**依赖：** Task 1-7

#### 8.1 用户文档（1 天）

**文档清单：**
1. 如何创建 Agent（图文教程）
2. 如何配置流程选择规则
3. 如何使用模板
4. 常见问题 FAQ

**验收标准：**
- [ ] 文档清晰易懂
- [ ] 有截图和示例
- [ ] 覆盖所有核心功能
- [ ] 有视频教程（可选）

#### 8.2 开发文档（1 天）

**文档清单：**
1. 数据库表结构
2. API 接口文档（Swagger）
3. 架构设计文档
4. 部署指南

**验收标准：**
- [ ] 文档完整
- [ ] API 文档自动生成
- [ ] 有代码示例

---

### Task 9: 运营监控模块（遗漏补充）

**工作量：** 3 天
**负责人：** 前端 + 后端开发
**依赖：** Task 1-5
**优先级：** P0

#### 9.1 任务执行记录页面（1 天）

**页面功能：**
- 任务列表（分页、搜索、筛选）
- 任务详情（执行过程、审计日志）
- 任务统计（成功率、平均耗时）

**API 接口：**
```typescript
// 获取任务列表
export const getTaskExecutionPage = (params: TaskPageReqVO) => {
  return request.get<PageResult<TaskExecutionVO>>({
    url: '/agentx/task/page',
    params
  })
}

// 获取任务详情
export const getTaskExecutionDetail = (id: number) => {
  return request.get<TaskExecutionDetailVO>({
    url: `/agentx/task/${id}`
  })
}
```

**验收标准：**
- [ ] 列表页面显示正常
- [ ] 支持按 Agent、状态、时间筛选
- [ ] 详情页显示完整执行过程
- [ ] 有执行时间线展示
- [ ] 性能：列表加载 < 1s

#### 9.2 审批记录查询（1 天）

**页面功能：**
- 审批记录列表
- 审批详情
- 审批统计

**验收标准：**
- [ ] 可以查询所有审批记录
- [ ] 支持按审批人、状态筛选
- [ ] 显示审批时间、结果、意见

#### 9.3 审计日志查询（1 天）

**页面功能：**
- 审计日志列表
- 日志详情
- 日志导出

**验收标准：**
- [ ] 可以查询所有操作日志
- [ ] 支持按操作人、操作类型筛选
- [ ] 支持导出为 Excel
- [ ] 敏感信息已脱敏

---

## Phase A 总结

**总工作量：** 28 天（5.5 周）

**任务清单：**
1. ✅ Agent 管理中心 UI - 5 天
2. ✅ 数字员工在组织架构中集成 - 3 天
3. ✅ Agent 与 Flowable 流程关联 - 3 天
4. ✅ 流程模板库 - 3 天
5. ✅ 后台自动化处理 - 3 天
6. ✅ 数据库表创建 - 1 天
7. ✅ 单元测试和集成测试 - 5 天
8. ✅ 文档编写 - 2 天
9. ✅ 运营监控模块 - 3 天

**关键里程碑：**
- Week 1: Task 1 + Task 6（UI 框架 + 数据库）
- Week 2: Task 2 + Task 3（组织架构 + 流程关联）
- Week 3: Task 4 + Task 5（模板库 + 自动化）
- Week 4: Task 9（运营监控）
- Week 5: Task 7 + Task 8（测试 + 文档）

**Phase A 完成后：**
- 用户可以在 3 分钟内创建 Agent
- 完全不需要输入技术 ID/JSON
- 数字员工在组织架构中可见
- 有完整的运营监控能力
- 为 Phase B 打下基础


---

### Task 10: Flowable 流程执行引擎（桥接层）

**工作量：** 4 天
**负责人：** 后端开发
**依赖：** Task 3
**优先级：** P0（严重缺失）

#### 10.1 实现 Flowable 事件监听器

**工作量：** 1.5 天

**核心设计：**
```java
@Component
public class AgentXFlowableEventListener implements FlowableEventListener {

    @Resource
    private AgentProcessMapper agentProcessMapper;
    
    @Resource
    private AiDecisionService aiDecisionService;
    
    @Resource
    private ToolCallService toolCallService;

    @Override
    public void onEvent(FlowableEvent event) {
        if (event.getType() == FlowableEngineEventType.ACTIVITY_STARTED) {
            handleActivityStarted((FlowableActivityEvent) event);
        }
    }

    private void handleActivityStarted(FlowableActivityEvent event) {
        String activityType = event.getActivityType();
        
        // 检查是否是 AgentX 扩展节点
        if ("agentx:aiDecision".equals(activityType)) {
            executeAiDecisionNode(event);
        } else if ("agentx:toolCall".equals(activityType)) {
            executeToolCallNode(event);
        } else if ("agentx:dataQuery".equals(activityType)) {
            executeDataQueryNode(event);
        } else if ("agentx:apiCall".equals(activityType)) {
            executeApiCallNode(event);
        }
    }

    private void executeAiDecisionNode(FlowableActivityEvent event) {
        DelegateExecution execution = event.getExecution();
        
        // 获取节点配置
        String prompt = getFieldValue(execution, "prompt");
        String inputVars = getFieldValue(execution, "inputVariables");
        
        // 构建上下文
        Map<String, Object> context = buildContext(execution, inputVars);
        
        // 调用 AI 决策
        String decision = aiDecisionService.decide(prompt, context);
        
        // 设置结果到流程变量
        execution.setVariable("aiDecision", decision);
        
        log.info("AI 决策完成: processInstanceId={}, decision={}", 
            execution.getProcessInstanceId(), decision);
    }
}
```

**注册监听器：**
```java
@Configuration
public class FlowableConfig {

    @Bean
    public ProcessEngineConfigurationConfigurer agentXEventListenerConfigurer(
        AgentXFlowableEventListener listener
    ) {
        return configuration -> {
            configuration.setEventListeners(Collections.singletonList(listener));
        };
    }
}
```

**验收标准：**
- [ ] 监听器可以正常注册到 Flowable
- [ ] 可以监听到流程节点启动事件
- [ ] 可以识别 AgentX 扩展节点类型
- [ ] 可以获取节点配置参数
- [ ] 可以设置执行结果到流程变量
- [ ] 有完整的日志记录
- [ ] 异常不会导致流程卡死

**测试用例：**
```java
@Test
public void testAiDecisionNodeExecution() {
    // 1. 创建包含 AI 决策节点的流程
    String processKey = deployTestProcess();
    
    // 2. 启动流程
    Map<String, Object> variables = new HashMap<>();
    variables.put("leaveDays", 3);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, variables);
    
    // 3. 验证 AI 决策节点被执行
    String decision = (String) runtimeService.getVariable(pi.getId(), "aiDecision");
    assertNotNull(decision);
    
    // 4. 验证流程继续执行
    assertFalse(runtimeService.createProcessInstanceQuery()
        .processInstanceId(pi.getId()).singleResult().isEnded());
}
```

---

#### 10.2 实现 AgentX 扩展节点 Delegate

**工作量：** 1.5 天

**AI 决策节点：**
```java
public class AiDecisionDelegate implements JavaDelegate {

    @Resource
    private LLMService llmService;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            // 1. 获取配置
            String prompt = getFieldValue(execution, "prompt");
            String inputVars = getFieldValue(execution, "inputVariables");
            Integer timeout = getFieldValue(execution, "timeout", 30);
            
            // 2. 构建上下文
            Map<String, Object> context = buildContext(execution, inputVars);
            
            // 3. 调用 LLM（带超时）
            String decision = llmService.decide(prompt, context, timeout);
            
            // 4. 设置结果
            execution.setVariable("aiDecision", decision);
            
            log.info("AI 决策成功: {}", decision);
            
        } catch (TimeoutException e) {
            log.error("AI 决策超时", e);
            execution.setVariable("aiDecisionError", "timeout");
            throw new BpmnError("AI_DECISION_TIMEOUT");
        } catch (Exception e) {
            log.error("AI 决策失败", e);
            execution.setVariable("aiDecisionError", e.getMessage());
            throw new BpmnError("AI_DECISION_FAILED");
        }
    }
    
    private Map<String, Object> buildContext(DelegateExecution execution, String inputVars) {
        Map<String, Object> context = new HashMap<>();
        if (StringUtils.hasText(inputVars)) {
            String[] vars = inputVars.split(",");
            for (String var : vars) {
                Object value = execution.getVariable(var.trim());
                context.put(var.trim(), value);
            }
        }
        return context;
    }
}
```

**Tool 调用节点：**
```java
public class ToolCallDelegate implements JavaDelegate {

    @Resource
    private ToolRegistry toolRegistry;
    
    @Resource
    private AgentCapabilityService capabilityService;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            // 1. 获取配置
            String toolName = getFieldValue(execution, "toolName");
            String paramsJson = getFieldValue(execution, "params");
            Long agentId = (Long) execution.getVariable("agentId");
            
            // 2. 权限检查
            if (!capabilityService.hasToolPermission(agentId, toolName)) {
                throw new BpmnError("TOOL_PERMISSION_DENIED");
            }
            
            // 3. 解析参数
            Map<String, Object> params = parseParams(paramsJson, execution);
            
            // 4. 调用 Tool
            Object result = toolRegistry.invoke(toolName, params);
            
            // 5. 设置结果
            execution.setVariable("toolResult", result);
            
            log.info("Tool 调用成功: tool={}, result={}", toolName, result);
            
        } catch (Exception e) {
            log.error("Tool 调用失败", e);
            execution.setVariable("toolError", e.getMessage());
            throw new BpmnError("TOOL_CALL_FAILED");
        }
    }
}
```

**验收标准：**
- [ ] AI 决策节点可以正常执行
- [ ] Tool 调用节点可以正常执行
- [ ] 支持超时控制（默认 30s）
- [ ] 权限检查正确
- [ ] 参数解析支持流程变量引用
- [ ] 异常处理完善（BpmnError）
- [ ] 单元测试覆盖率 > 80%

---

#### 10.3 实现流程与 Agent 的关联执行

**工作量：** 1 天

**Service 实现：**
```java
@Service
public class AgentProcessExecutionService {

    @Resource
    private RuntimeService runtimeService;
    
    @Resource
    private ProcessSelectionService processSelectionService;

    /**
     * Agent 执行流程
     */
    public String executeProcess(Long agentId, Map<String, Object> taskContext) {
        // 1. 选择合适的流程
        String processKey = processSelectionService.selectProcess(agentId, taskContext);
        
        // 2. 准备流程变量
        Map<String, Object> variables = new HashMap<>(taskContext);
        variables.put("agentId", agentId);
        variables.put("startTime", LocalDateTime.now());
        
        // 3. 启动流程实例
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(processKey, variables);
        
        log.info("Agent 启动流程: agentId={}, processKey={}, processInstanceId={}", 
            agentId, processKey, processInstance.getId());
        
        return processInstance.getId();
    }

    /**
     * 查询流程执行状态
     */
    public ProcessExecutionStatus getProcessStatus(String processInstanceId) {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        
        if (pi == null) {
            // 流程已结束
            HistoricProcessInstance hpi = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
            
            return ProcessExecutionStatus.builder()
                .processInstanceId(processInstanceId)
                .status("completed")
                .endTime(hpi.getEndTime())
                .build();
        }
        
        // 流程运行中
        return ProcessExecutionStatus.builder()
            .processInstanceId(processInstanceId)
            .status("running")
            .currentActivityId(pi.getActivityId())
            .build();
    }
}
```

**验收标准：**
- [ ] 可以启动 Flowable 流程
- [ ] agentId 正确传递到流程变量
- [ ] 可以查询流程执行状态
- [ ] 流程结束后可以获取历史记录
- [ ] 单元测试通过

---

#### 10.4 集成测试

**工作量：** 0.5 天

**测试场景：完整的 Agent 执行流程**
```java
@Test
public void testCompleteAgentProcessExecution() {
    // 1. 创建 Agent
    Long agentId = createTestAgent();
    
    // 2. 部署包含 AgentX 节点的流程
    String processKey = deployProcessWithAgentXNodes();
    
    // 3. 关联 Agent 和流程
    associateAgentWithProcess(agentId, processKey);
    
    // 4. Agent 执行流程
    Map<String, Object> context = Map.of(
        "leaveDays", 3,
        "leaveType", "annual"
    );
    String processInstanceId = agentProcessExecutionService
        .executeProcess(agentId, context);
    
    // 5. 验证流程启动成功
    assertNotNull(processInstanceId);
    
    // 6. 验证 AI 决策节点被执行
    String decision = (String) runtimeService
        .getVariable(processInstanceId, "aiDecision");
    assertNotNull(decision);
    
    // 7. 验证 Tool 调用节点被执行
    Object toolResult = runtimeService
        .getVariable(processInstanceId, "toolResult");
    assertNotNull(toolResult);
    
    // 8. 验证流程状态
    ProcessExecutionStatus status = agentProcessExecutionService
        .getProcessStatus(processInstanceId);
    assertEquals("running", status.getStatus());
}
```

**验收标准：**
- [ ] 完整流程测试通过
- [ ] Agent → 流程选择 → 流程执行 → 节点处理 全链路正常
- [ ] 异常场景有测试覆盖
- [ ] 测试可自动化运行

---

**Task 10 总结：**

**完成后效果：**
- ✅ Agent 可以执行 Flowable 流程
- ✅ Flowable 流程可以使用 AgentX 扩展节点
- ✅ AI 决策和 Tool 调用无缝集成
- ✅ 完整的事件监听和错误处理

**工作量：** 4 天

---
