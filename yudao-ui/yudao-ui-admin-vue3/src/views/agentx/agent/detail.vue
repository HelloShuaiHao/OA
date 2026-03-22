<template>
  <ContentWrap>
    <el-page-header content="数字员工详情" @back="router.push('/agentx/agent/list')" />
  </ContentWrap>

  <ContentWrap v-loading="loading">
    <el-descriptions border :column="2" title="基本信息">
      <el-descriptions-item label="员工名称">{{ detail?.agentName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusLabel(detail?.status) }}</el-descriptions-item>
      <el-descriptions-item label="部门">{{ detail?.deptName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="模板">{{ detail?.templateType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="员工 Key" :span="2">{{ detail?.agentKey || '-' }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ detail?.description || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDate(detail?.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="最后更新时间">{{ formatDate(detail?.updateTime) }}</el-descriptions-item>
      <el-descriptions-item label="创建人">{{ detail?.creator || '-' }}</el-descriptions-item>
      <el-descriptions-item label="更新人">{{ detail?.updater || '-' }}</el-descriptions-item>
    </el-descriptions>
  </ContentWrap>

  <ContentWrap>
    <div class="action-bar">
      <el-button
        type="primary"
        @click="router.push(`/agentx/agent/create?id=${currentAgentId}`)"
        v-hasPermi="['agentx:agent:update']"
      >
        编辑
      </el-button>
      <el-button type="primary" plain @click="router.push('/agentx/channel')">渠道配置</el-button>
      <el-button type="warning" @click="handleStatus" v-hasPermi="['agentx:agent:update']">
        {{ detail?.status === 1 ? '停用' : '启用' }}
      </el-button>
      <el-button type="danger" @click="handleDelete" v-hasPermi="['agentx:agent:delete']">删除</el-button>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-card shadow="never">
      <template #header>能力配置</template>
      <el-empty v-if="!detail?.capabilities?.length" description="暂无能力配置" />
      <el-table v-else :data="detail?.capabilities">
        <el-table-column label="能力 Key" prop="capabilityKey" min-width="180" />
        <el-table-column label="能力名称" prop="capabilityName" min-width="180" />
        <el-table-column label="启用" prop="enabled" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </ContentWrap>

  <ContentWrap>
    <el-card shadow="never">
      <template #header>关联流程</template>
      <el-empty v-if="!detail?.processes?.length" description="暂无关联流程" />
      <el-table v-else :data="detail?.processes">
        <el-table-column label="流程名称" prop="processName" min-width="220" />
        <el-table-column label="流程 Key" prop="processDefinitionKey" min-width="180" />
        <el-table-column label="流程版本" prop="processVersion" width="100" align="center" />
      </el-table>
      <div class="strategy-text">
        策略：{{ detail?.selectionMode === 'auto' ? 'AI 自动选择' : '规则选择' }}，规则数：{{ detail?.selectionRules?.length || 0 }}
      </div>
      <el-table v-if="detail?.selectionRules?.length" :data="selectionRuleRows" class="mt-10px">
        <el-table-column label="字段" prop="field" width="140" />
        <el-table-column label="操作符" prop="operator" width="100" align="center" />
        <el-table-column label="值" prop="value" min-width="140" />
        <el-table-column label="目标流程" prop="processName" min-width="220" />
      </el-table>
    </el-card>
  </ContentWrap>

  <ContentWrap>
    <el-card shadow="never">
      <template #header>组织架构成员（含数字员工）</template>
      <el-empty v-if="!deptMembers.length" description="暂无成员" />
      <el-table v-else :data="deptMembers">
        <el-table-column label="昵称" prop="nickname" min-width="180" />
        <el-table-column label="账号" prop="username" min-width="160" />
        <el-table-column label="类型" prop="userType" width="140" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.userType === 'agent' ? 'warning' : 'info'">
              {{ scope.row.userType === 'agent' ? '数字员工' : '真实员工' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Agent ID" prop="agentId" width="120" align="center" />
      </el-table>
    </el-card>
  </ContentWrap>
</template>

<script setup lang="ts">
import * as AgentApi from '@/api/agentx/agent'
import * as DeptApi from '@/api/system/dept'

defineOptions({ name: 'AgentxAgentDetail' })

const route = useRoute()
const router = useRouter()
const message = useMessage()
const loading = ref(false)
const detail = ref<AgentApi.AgentVO>()
const deptMembers = ref<DeptApi.DeptMemberVO[]>([])
const currentAgentId = computed(() => Number(route.params.id || route.query.id || 0))
const selectionRuleRows = computed(() => {
  const processMap = Object.fromEntries(
    (detail.value?.processes || []).map((p) => [p.processDefinitionId, p.processName || p.processDefinitionKey || p.processDefinitionId])
  )
  return (detail.value?.selectionRules || []).map((rule) => ({
    ...rule,
    processName: processMap[rule.processDefinitionId] || rule.processDefinitionId
  }))
})

const statusLabel = (status?: number) => {
  if (status === 1) return '激活'
  if (status === 2) return '停用'
  return '草稿'
}

const formatDate = (value?: string) => {
  return value ? formatDateTime(value) : '-'
}

const getDetail = async () => {
  const id = currentAgentId.value
  if (!id) return
  loading.value = true
  try {
    detail.value = await AgentApi.getAgent(id)
    if (detail.value?.deptId) {
      deptMembers.value = await DeptApi.getDeptMembers(detail.value.deptId, 'all')
    } else {
      deptMembers.value = []
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getDetail()
})

const handleStatus = async () => {
  if (!detail.value?.id) return
  const nextStatus = detail.value.status === 1 ? 2 : 1
  await AgentApi.updateAgentStatus(detail.value.id, nextStatus)
  message.success('状态更新成功')
  await getDetail()
}

const handleDelete = async () => {
  if (!detail.value?.id) return
  await message.delConfirm()
  await AgentApi.deleteAgent(detail.value.id)
  message.success('删除成功')
  router.push('/agentx/agent/list')
}
</script>

<style scoped>
.action-bar {
  display: flex;
  gap: 10px;
}

.strategy-text {
  margin-top: 10px;
  color: var(--el-text-color-secondary);
}
</style>
