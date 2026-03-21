<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      class="-mb-15px"
      :model="queryParams"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="实例名称" prop="instanceName">
        <el-input
          v-model="queryParams.instanceName"
          placeholder="请输入实例名称"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="实例地址" prop="endpoint">
        <el-input
          v-model="queryParams.endpoint"
          placeholder="请输入实例地址"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-140px">
          <el-option :value="1" label="在线" />
          <el-option :value="0" label="离线" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['agentx:instance:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="90" />
      <el-table-column label="实例名称" align="center" prop="instanceName" min-width="180" />
      <el-table-column label="实例地址" align="center" prop="endpoint" min-width="260" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="120" />
      <el-table-column
        label="最近心跳"
        align="center"
        prop="lastHeartbeat"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="260">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['agentx:instance:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="handleTestConnection(scope.row.id)"
            v-hasPermi="['agentx:instance:test']"
          >
            测试连接
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['agentx:instance:delete']"
          >
            删除
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

  <InstanceForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as OpenfangInstanceApi from '@/api/agentx/instance'
import InstanceForm from './InstanceForm.vue'

defineOptions({ name: 'AgentxInstance' })

const { t } = useI18n()
const message = useMessage()

const loading = ref(false)
const total = ref(0)
const list = ref<OpenfangInstanceApi.OpenfangInstanceVO[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  instanceName: '',
  endpoint: '',
  status: undefined as undefined | number
})

const getList = async () => {
  loading.value = true
  try {
    const data = await OpenfangInstanceApi.getInstancePage(queryParams)
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

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await OpenfangInstanceApi.deleteInstance(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleTestConnection = async (id: number) => {
  loading.value = true
  try {
    const data = await OpenfangInstanceApi.testConnection({ id })
    if (data.online) {
      message.success(`连接成功${data.version ? `，版本 ${data.version}` : ''}`)
    } else {
      message.error(data.message || '连接失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
