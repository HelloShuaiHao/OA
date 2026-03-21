<template>
  <ContentWrap>
    <el-form ref="queryFormRef" class="-mb-15px" :model="queryParams" :inline="true" label-width="68px">
      <el-form-item label="场景编码" prop="scenarioCode">
        <el-input
          v-model="queryParams.scenarioCode"
          placeholder="请输入场景编码"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务键" prop="businessKey">
        <el-input
          v-model="queryParams.businessKey"
          placeholder="请输入业务键"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="projectionStatus">
        <el-select v-model="queryParams.projectionStatus" placeholder="请选择状态" clearable style="width: 160px">
          <el-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="90" />
      <el-table-column label="场景编码" align="center" prop="scenarioCode" min-width="160" />
      <el-table-column label="业务键" align="center" prop="businessKey" min-width="180" />
      <el-table-column label="TaskRunId" align="center" prop="openfangTaskRunId" min-width="180" />
      <el-table-column label="状态" align="center" prop="projectionStatus" width="120">
        <template #default="scope">
          <el-tag>{{ statusLabel(scope.row.projectionStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果摘要" align="center" prop="resultSummary" min-width="220" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="120">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openRuntime(scope.row)"
            :disabled="!scope.row.openfangTaskRunId"
            v-hasPermi="['agentx:task:query']"
          >
            运行时
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <el-dialog v-model="runtimeVisible" title="运行时详情" width="800px">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="TaskRunId">{{ runtimeDetail?.taskRunId }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ runtimeDetail?.status }}</el-descriptions-item>
      <el-descriptions-item label="阶段">{{ runtimeDetail?.stage }}</el-descriptions-item>
      <el-descriptions-item label="Workflow">{{ runtimeDetail?.workflowId }}</el-descriptions-item>
      <el-descriptions-item label="待审批ID" :span="2">
        {{ (runtimeDetail?.pendingApprovalIds || []).join(', ') || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="结果摘要" :span="2">{{ runtimeDetail?.resultSummary || '-' }}</el-descriptions-item>
      <el-descriptions-item label="失败摘要" :span="2">{{ runtimeDetail?.failureSummary || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">Trace Events</el-divider>
    <pre class="runtime-json">{{ JSON.stringify(runtimeDetail?.traceEvents || [], null, 2) }}</pre>

    <el-divider content-position="left">Workflow Projections</el-divider>
    <pre class="runtime-json">{{ JSON.stringify(runtimeDetail?.workflowProjections || [], null, 2) }}</pre>
  </el-dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as AgentxTaskApi from '@/api/agentx/task'

defineOptions({ name: 'AgentxTask' })

const loading = ref(false)
const total = ref(0)
const list = ref<AgentxTaskApi.AgentxTaskProjectionVO[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  scenarioCode: '',
  businessKey: '',
  projectionStatus: undefined as undefined | number
})

const statusOptions = [
  { value: 0, label: 'CREATED' },
  { value: 10, label: 'RUNNING' },
  { value: 20, label: 'WAITING_APPROVAL' },
  { value: 30, label: 'APPROVED' },
  { value: 40, label: 'REJECTED' },
  { value: 50, label: 'SUCCEEDED' },
  { value: 60, label: 'FAILED' },
  { value: 70, label: 'COMPENSATED' }
]

const statusLabel = (status: number) => {
  const item = statusOptions.find((i) => i.value === status)
  return item ? item.label : String(status)
}

const getList = async () => {
  loading.value = true
  try {
    const data = await AgentxTaskApi.getTaskPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const runtimeVisible = ref(false)
const runtimeDetail = ref<AgentxTaskApi.AgentxTaskRuntimeVO>()
const openRuntime = async (row: AgentxTaskApi.AgentxTaskProjectionVO) => {
  if (!row.openfangTaskRunId) return
  runtimeDetail.value = await AgentxTaskApi.getTaskRuntime(row.openfangTaskRunId)
  runtimeVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.runtime-json {
  max-height: 260px;
  overflow: auto;
  font-size: 12px;
  padding: 12px;
  background: #f7f8fa;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}
</style>
