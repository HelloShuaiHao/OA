<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" inline label-width="76px" class="-mb-15px">
      <el-form-item label="渠道类型" prop="channelType">
        <el-select v-model="queryParams.channelType" clearable placeholder="请选择类型" style="width: 160px">
          <el-option label="Telegram" value="telegram" />
          <el-option label="企业微信" value="wecom" />
          <el-option label="钉钉" value="dingtalk" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="channelName">
        <el-input v-model="queryParams.channelName" clearable placeholder="请输入渠道名称" style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['agentx:channel:create']">
          <Icon icon="ep:plus" class="mr-5px" />添加渠道
        </el-button>
        <el-button type="success" plain @click="router.push('/agentx/channel/binding')">
          <Icon icon="ep:link" class="mr-5px" />绑定管理
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" prop="id" width="90" align="center" />
      <el-table-column label="类型" prop="channelType" width="120" />
      <el-table-column label="名称" prop="channelName" min-width="160" />
      <el-table-column label="关联 Agent" prop="agentIds" min-width="220">
        <template #default="scope">{{ (scope.row.agentIds || []).join(', ') || '-' }}</template>
      </el-table-column>
      <el-table-column label="访问控制" prop="accessControlType" width="120" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="openForm(scope.row.id)" v-hasPermi="['agentx:channel:update']">编辑</el-button>
          <el-button link type="success" @click="testConnection(scope.row)" v-hasPermi="['agentx:channel:update']">测试连接</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['agentx:channel:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="formData.id ? '编辑渠道' : '新增渠道'" width="680px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
      <el-form-item label="渠道类型" prop="channelType">
        <el-select v-model="formData.channelType" style="width: 100%">
          <el-option label="Telegram" value="telegram" />
          <el-option label="企业微信" value="wecom" />
          <el-option label="钉钉" value="dingtalk" />
        </el-select>
      </el-form-item>
      <el-form-item label="渠道名称" prop="channelName">
        <el-input v-model="formData.channelName" />
      </el-form-item>
      <el-form-item label="Bot Token" prop="botToken">
        <el-input v-model="formData.botToken" show-password />
      </el-form-item>
      <el-form-item label="关联 Agent" prop="agentIds">
        <el-select v-model="formData.agentIds" multiple filterable clearable style="width: 100%">
          <el-option v-for="item in agentOptions" :key="item.id" :label="item.agentName" :value="item.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="访问控制" prop="accessControlType">
        <el-radio-group v-model="formData.accessControlType">
          <el-radio v-for="item in accessControlOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
        <div class="mt-8px text-12px text-[var(--el-text-color-secondary)]">
          {{ accessControlHint }}
        </div>
      </el-form-item>
      <el-form-item v-if="formData.accessControlType === 'dept'" label="部门" prop="deptIds">
        <el-select v-model="formData.deptIds" multiple filterable clearable style="width: 100%">
          <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="formData.agentIds?.length" label="Agent 认证">
        <div style="width: 100%">
          <div
            v-for="agentId in formData.agentIds"
            :key="agentId"
            style="display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 10px"
          >
            <span>{{ resolveAgentName(agentId) }}</span>
            <el-select
              :model-value="getAgentAuthMode(agentId)"
              style="width: 220px"
              @update:model-value="(val) => setAgentAuthMode(agentId, val as 'public' | 'bind_required')"
            >
              <el-option label="公开访问（适合销售/外部咨询）" value="public" />
              <el-option label="必须绑定（适合公司内部 Agent）" value="bind_required" />
            </el-select>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-switch v-model="statusEnabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">保存</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as ChannelApi from '@/api/agentx/channel'
import * as AgentApi from '@/api/agentx/agent'
import * as DeptApi from '@/api/system/dept'

defineOptions({ name: 'AgentxChannelConfig' })

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const total = ref(0)
const list = ref<ChannelApi.AgentxChannelConfigVO[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  channelType: undefined as undefined | string,
  channelName: ''
})

const dialogVisible = ref(false)
const formRef = ref()
const formData = reactive<ChannelApi.AgentxChannelConfigVO>({
  channelType: 'telegram',
  channelName: '',
  botToken: '',
  agentIds: [],
  accessControlType: 'all',
  deptIds: [],
  userIds: [],
  agentAccessPolicies: [],
  status: 0
})
const statusEnabled = computed({
  get: () => formData.status === 0,
  set: (val: boolean) => (formData.status = val ? 0 : 1)
})
const formRules = reactive({
  channelType: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
  channelName: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  botToken: [
    {
      validator: (_: any, value: string, callback: (error?: Error) => void) => {
        if (!formData.id && !value) {
          callback(new Error('请输入 Bot Token'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  agentIds: [{ required: true, message: '请至少关联一个 Agent', trigger: 'change' }]
})

const agentOptions = ref<any[]>([])
const deptOptions = ref<DeptApi.DeptVO[]>([])
const requiresBoundAccess = computed(() =>
  (formData.agentAccessPolicies || []).some((item) => item.authMode === 'bind_required')
)
const hasPublicAccess = computed(() =>
  (formData.agentAccessPolicies || []).some((item) => item.authMode === 'public')
)
const accessControlOptions = computed(() => {
  if (requiresBoundAccess.value) {
    return [
      { value: 'all', label: '所有已绑定员工' },
      { value: 'dept', label: '指定部门员工' },
      { value: 'user', label: '指定人员' }
    ]
  }
  return [
    { value: 'all', label: '所有访问者' },
    { value: 'dept', label: '指定部门员工' },
    { value: 'user', label: '指定人员' }
  ]
})
const accessControlHint = computed(() => {
  if (!formData.agentIds?.length) {
    return '先选择关联 Agent，再配置访问范围和认证方式。'
  }
  if (requiresBoundAccess.value && hasPublicAccess.value) {
    return '访问控制只作用于“必须绑定”的 Agent；公开访问的 Agent 允许匿名访问。'
  }
  if (requiresBoundAccess.value) {
    return '访问控制作用于绑定后的 OA 员工范围，外部用户需先完成身份绑定。'
  }
  return '当前 Agent 允许匿名访问；这里配置的是可进一步放行的内部员工范围。'
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ChannelApi.getChannelConfigPage(queryParams as any)
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
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openForm = async (id?: number) => {
  dialogVisible.value = true
  formData.id = undefined
  formData.channelType = 'telegram'
  formData.channelName = ''
  formData.botToken = ''
  formData.agentIds = []
  formData.accessControlType = 'all'
  formData.deptIds = []
  formData.userIds = []
  formData.agentAccessPolicies = []
  formData.status = 0
  if (id) {
    const data = await ChannelApi.getChannelConfig(id)
    Object.assign(formData, data)
  }
  syncAgentAccessPolicies()
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  if (formData.id) {
    await ChannelApi.updateChannelConfig(formData)
    message.success('修改成功')
  } else {
    await ChannelApi.createChannelConfig(formData)
    message.success('新增成功')
  }
  dialogVisible.value = false
  await getList()
}

const handleDelete = async (id: number) => {
  await message.delConfirm()
  await ChannelApi.deleteChannelConfig(id)
  message.success('删除成功')
  await getList()
}

const testConnection = async (row: ChannelApi.AgentxChannelConfigVO) => {
  const data = await ChannelApi.testChannelConfig({ channelId: row.id, channelType: row.channelType })
  if (data.success) {
    message.success(data.message)
  } else {
    message.warning(data.message)
  }
}

onMounted(async () => {
  const [agents, depts] = await Promise.all([
    AgentApi.getAgentPage({ pageNo: 1, pageSize: 200 } as any),
    DeptApi.getSimpleDeptList()
  ])
  agentOptions.value = agents.list || []
  deptOptions.value = depts || []
  await getList()
})

const resolveAgentName = (agentId: number) => {
  return agentOptions.value.find((item) => item.id === agentId)?.agentName || `Agent ${agentId}`
}

const syncAgentAccessPolicies = () => {
  const existing = new Map((formData.agentAccessPolicies || []).map((item) => [item.agentId, item.authMode]))
  formData.agentAccessPolicies = (formData.agentIds || []).map((agentId) => ({
    agentId,
    authMode: existing.get(agentId) || 'bind_required'
  }))
}

const getAgentAuthMode = (agentId: number) => {
  return formData.agentAccessPolicies?.find((item) => item.agentId === agentId)?.authMode || 'bind_required'
}

const setAgentAuthMode = (agentId: number, authMode: 'public' | 'bind_required') => {
  syncAgentAccessPolicies()
  const target = formData.agentAccessPolicies?.find((item) => item.agentId === agentId)
  if (target) {
    target.authMode = authMode
  }
}

watch(
  () => [...(formData.agentIds || [])],
  () => {
    syncAgentAccessPolicies()
  }
)
</script>
