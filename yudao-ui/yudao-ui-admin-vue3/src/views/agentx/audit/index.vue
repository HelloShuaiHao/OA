<template>
  <ContentWrap>
    <el-form ref="queryFormRef" class="-mb-15px" :model="queryParams" :inline="true" label-width="78px">
      <el-form-item label="事件类型" prop="eventType">
        <el-input v-model="queryParams.eventType" clearable placeholder="请输入事件类型" style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="场景编码" prop="scenarioCode">
        <el-input v-model="queryParams.scenarioCode" clearable placeholder="请输入场景编码" style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务键" prop="businessKey">
        <el-input v-model="queryParams.businessKey" clearable placeholder="请输入业务键" style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工具" prop="toolName">
        <el-input v-model="queryParams.toolName" clearable placeholder="请输入工具名" style="width: 180px" @keyup.enter="handleQuery" />
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
      <el-table-column label="事件类型" prop="eventType" align="center" width="170" />
      <el-table-column label="场景编码" prop="scenarioCode" align="center" min-width="140" />
      <el-table-column label="业务键" prop="businessKey" align="center" min-width="160" />
      <el-table-column label="工具" prop="toolName" align="center" width="150" />
      <el-table-column label="错误码" prop="errorCode" align="center" width="100" />
      <el-table-column label="请求摘要" prop="requestSummary" align="center" min-width="220" show-overflow-tooltip />
      <el-table-column label="结果摘要" prop="resultSummary" align="center" min-width="220" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" align="center" width="180" :formatter="dateFormatter" />
    </el-table>

    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as AgentxAuditApi from '@/api/agentx/audit'

defineOptions({ name: 'AgentxAudit' })

const loading = ref(false)
const total = ref(0)
const list = ref<AgentxAuditApi.AgentxAuditEventVO[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  eventType: '',
  scenarioCode: '',
  businessKey: '',
  toolName: ''
})

const getList = async () => {
  loading.value = true
  try {
    const data = await AgentxAuditApi.getAuditPage(queryParams)
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
