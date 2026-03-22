<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" :inline="true" label-width="80px">
      <el-form-item label="员工名称">
        <el-input
          v-model="queryParams.agentName"
          placeholder="请输入员工名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 160px">
          <el-option :value="0" label="草稿" />
          <el-option :value="1" label="激活" />
          <el-option :value="2" label="停用" />
        </el-select>
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="queryParams.deptId" filterable placeholder="请选择部门" clearable style="width: 220px">
          <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="模板">
        <el-select v-model="queryParams.templateType" placeholder="请选择模板" clearable style="width: 180px">
          <el-option v-for="item in templateOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="router.push('/agentx/agent/create')"
          v-hasPermi="['agentx:agent:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 创建数字员工
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-empty v-if="!loading && list.length === 0" description="暂无数字员工" />

    <div v-else class="card-grid" v-loading="loading">
      <el-card v-for="item in list" :key="item.id" shadow="hover" class="agent-card" @click="openDetail(item.id!)">
        <div class="card-head">
          <el-avatar :size="44" :src="item.avatarUrl">{{ item.agentName.slice(0, 1) }}</el-avatar>
          <div class="head-main">
            <div class="name-row">
              <span class="agent-name">{{ item.agentName }}</span>
              <el-tag :type="statusType(item.status)">
                {{ statusLabel(item.status) }}
              </el-tag>
            </div>
            <div class="dept-text">{{ item.deptName || '-' }}</div>
          </div>
        </div>

        <div class="desc-text">{{ item.description || '-' }}</div>

        <div class="card-actions">
          <el-button link type="primary" @click.stop="openDetail(item.id!)">详情</el-button>
          <el-button link type="primary" @click.stop="openEdit(item.id!)" v-hasPermi="['agentx:agent:update']">编辑</el-button>
          <el-button
            link
            type="warning"
            @click.stop="handleStatus(item)"
            v-hasPermi="['agentx:agent:update']"
          >
            {{ item.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click.stop="handleDelete(item.id!)" v-hasPermi="['agentx:agent:delete']">
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import * as AgentApi from '@/api/agentx/agent'
import * as DeptApi from '@/api/system/dept'
import { useDebounceFn } from '@vueuse/core'

defineOptions({ name: 'AgentxAgentList' })

const message = useMessage()
const router = useRouter()
const loading = ref(false)
const total = ref(0)
const list = ref<AgentApi.AgentVO[]>([])
const deptOptions = ref<DeptApi.DeptVO[]>([])
const templateOptions = [
  { label: '请假审批助手', value: 'leave' },
  { label: '报销审批助手', value: 'expense' },
  { label: '采购审批助手', value: 'procurement' },
  { label: '客户跟进助手', value: 'crm' },
  { label: '数据分析助手', value: 'analysis' },
  { label: '文档处理助手', value: 'doc' },
  { label: '自定义', value: 'custom' }
]

const queryParams = reactive<AgentApi.AgentPageReqVO>({
  pageNo: 1,
  pageSize: 12,
  agentName: '',
  status: undefined,
  deptId: undefined,
  templateType: ''
})

const statusLabel = (status?: number) => {
  if (status === 1) return '激活'
  if (status === 2) return '停用'
  return '草稿'
}

const statusType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await AgentApi.getAgentPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.pageNo = 1
  queryParams.agentName = ''
  queryParams.status = undefined
  queryParams.deptId = undefined
  queryParams.templateType = ''
  getList()
}

const openDetail = (id: number) => {
  router.push(`/agentx/agent/detail/${id}`)
}

const openEdit = (id: number) => {
  router.push(`/agentx/agent/create?id=${id}`)
}

const handleStatus = async (item: AgentApi.AgentVO) => {
  const nextStatus = item.status === 1 ? 2 : 1
  await AgentApi.updateAgentStatus(item.id!, nextStatus)
  message.success('状态更新成功')
  getList()
}

const handleDelete = async (id: number) => {
  await message.delConfirm()
  await AgentApi.deleteAgent(id)
  message.success('删除成功')
  getList()
}

onMounted(() => {
  DeptApi.getSimpleDeptList().then((res) => {
    deptOptions.value = res || []
  })
  getList()
})

watch(
  () => queryParams.agentName,
  useDebounceFn(() => {
    queryParams.pageNo = 1
    getList()
  }, 500)
)
</script>

<style scoped>
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.agent-card {
  min-height: 180px;
  cursor: pointer;
}

.card-head {
  display: flex;
  gap: 10px;
  align-items: center;
}

.head-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.agent-name {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-text {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.desc-text {
  margin-top: 12px;
  min-height: 42px;
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.card-actions {
  margin-top: 10px;
}
</style>
