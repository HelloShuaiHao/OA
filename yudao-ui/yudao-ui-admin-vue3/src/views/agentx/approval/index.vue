<template>
  <ContentWrap>
    <el-form ref="queryFormRef" class="-mb-15px" :model="queryParams" :inline="true" label-width="76px">
      <el-form-item label="场景编码" prop="scenarioCode">
        <el-input v-model="queryParams.scenarioCode" clearable placeholder="请输入场景编码" style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务键" prop="businessKey">
        <el-input v-model="queryParams.businessKey" clearable placeholder="请输入业务键" style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="决策状态" prop="decisionStatus">
        <el-select v-model="queryParams.decisionStatus" clearable placeholder="请选择状态" style="width: 160px">
          <el-option v-for="item in decisionOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="回调失败" prop="callbackFailed">
        <el-select v-model="queryParams.callbackFailed" clearable placeholder="请选择" style="width: 120px">
          <el-option :value="true" label="是" />
          <el-option :value="false" label="否" />
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
      <el-table-column label="编号" prop="id" align="center" width="90" />
      <el-table-column label="场景编码" prop="scenarioCode" align="center" min-width="150" />
      <el-table-column label="业务键" prop="businessKey" align="center" min-width="180" />
      <el-table-column label="审批ID" prop="openfangApprovalId" align="center" min-width="160" />
      <el-table-column label="决策状态" prop="decisionStatus" align="center" width="120">
        <template #default="scope">{{ decisionLabel(scope.row.decisionStatus) }}</template>
      </el-table-column>
      <el-table-column label="回调重试" prop="callbackRetryCount" align="center" width="100" />
      <el-table-column label="回调失败" prop="callbackFailed" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.callbackFailed ? 'danger' : 'success'">{{ scope.row.callbackFailed ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" prop="callbackLastError" align="center" min-width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" align="center" width="180" :formatter="dateFormatter" />
    </el-table>

    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as ApprovalApi from '@/api/agentx/approval'

defineOptions({ name: 'AgentxApproval' })

const loading = ref(false)
const total = ref(0)
const list = ref<ApprovalApi.AgentxApprovalBindingVO[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  scenarioCode: '',
  businessKey: '',
  decisionStatus: undefined as undefined | number,
  callbackFailed: undefined as undefined | boolean
})

const decisionOptions = [
  { value: 10, label: 'APPROVED' },
  { value: 20, label: 'REJECTED' },
  { value: 30, label: 'WITHDRAWN' },
  { value: 40, label: 'TIMEOUT' },
  { value: 50, label: 'CANCELLED' }
]

const decisionLabel = (status?: number) => {
  const item = decisionOptions.find((d) => d.value === status)
  return item ? item.label : status ?? '-'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalApi.getApprovalPage(queryParams)
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

onMounted(() => {
  getList()
})
</script>
