<template>
  <ContentWrap>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的绑定" name="mine">
        <el-table v-loading="myLoading" :data="myBindings">
          <el-table-column label="编号" prop="id" width="90" align="center" />
          <el-table-column label="渠道" min-width="120">
            <template #default="scope">{{ channelLabel(scope.row.channelType) }}</template>
          </el-table-column>
          <el-table-column label="渠道用户 ID" prop="channelUserId" min-width="180" />
          <el-table-column label="渠道用户名" prop="channelUsername" min-width="160" />
          <el-table-column label="绑定时间" prop="bindTime" width="180" :formatter="dateFormatter" />
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button link type="danger" @click="handleMyUnbind(scope.row.id)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="管理端绑定关系" name="admin" v-if="hasPermission(['agentx:channel:query'])">
        <el-form :model="queryParams" inline label-width="76px" class="-mb-15px">
          <el-form-item label="用户 ID" prop="userId">
            <el-input-number v-model="queryParams.userId" :min="1" :controls="false" placeholder="请输入用户 ID" style="width: 180px" />
          </el-form-item>
          <el-form-item label="渠道类型" prop="channelType">
            <el-select v-model="queryParams.channelType" clearable placeholder="请选择类型" style="width: 160px">
              <el-option label="Telegram" value="telegram" />
              <el-option label="WhatsApp" value="whatsapp" />
              <el-option label="企业微信" value="wecom" />
              <el-option label="钉钉" value="dingtalk" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 130px">
              <el-option label="已绑定" :value="1" />
              <el-option label="已解绑" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="adminLoading" :data="adminList">
          <el-table-column label="编号" prop="id" width="90" align="center" />
          <el-table-column label="用户 ID" prop="userId" width="110" />
          <el-table-column label="渠道" min-width="120">
            <template #default="scope">{{ channelLabel(scope.row.channelType) }}</template>
          </el-table-column>
          <el-table-column label="渠道用户 ID" prop="channelUserId" min-width="180" />
          <el-table-column label="渠道用户名" prop="channelUsername" min-width="160" />
          <el-table-column label="状态" prop="status" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '已绑定' : '已解绑' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="绑定时间" prop="bindTime" width="180" :formatter="dateFormatter" />
          <el-table-column label="解绑时间" prop="unbindTime" width="180" :formatter="dateFormatter" />
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button
                link
                type="danger"
                :disabled="scope.row.status !== 1"
                @click="handleAdminUnbind(scope.row.id)"
                v-hasPermi="['agentx:channel:update']"
              >
                管理解绑
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getAdminList"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { hasPermission } from '@/utils/permission'
import * as ChannelApi from '@/api/agentx/channel'

defineOptions({ name: 'AgentxChannelBinding' })

const message = useMessage()
const route = useRoute()
const activeTab = ref('mine')

const myLoading = ref(false)
const myBindings = ref<ChannelApi.AgentxUserChannelBindingVO[]>([])

const adminLoading = ref(false)
const total = ref(0)
const adminList = ref<ChannelApi.AgentxUserChannelBindingVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined as number | undefined,
  channelType: undefined as string | undefined,
  status: undefined as number | undefined
})

const channelLabel = (channelType: string) => {
  if (channelType === 'telegram') return 'Telegram'
  if (channelType === 'whatsapp') return 'WhatsApp'
  if (channelType === 'wecom') return '企业微信'
  if (channelType === 'dingtalk') return '钉钉'
  return channelType
}

const getMyBindings = async () => {
  myLoading.value = true
  try {
    myBindings.value = (await ChannelApi.getMyBindings()) || []
  } finally {
    myLoading.value = false
  }
}

const getAdminList = async () => {
  if (!hasPermission(['agentx:channel:query'])) return
  adminLoading.value = true
  try {
    const data = await ChannelApi.getBindingPage(queryParams)
    adminList.value = data.list || []
    total.value = data.total || 0
  } finally {
    adminLoading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getAdminList()
}

const resetQuery = async () => {
  queryParams.pageNo = 1
  queryParams.userId = undefined
  queryParams.channelType = undefined
  queryParams.status = undefined
  await getAdminList()
}

const handleMyUnbind = async (id: number) => {
  await message.delConfirm('确认解绑当前渠道吗？')
  await ChannelApi.unbind(id)
  message.success('解绑成功')
  await getMyBindings()
}

const handleAdminUnbind = async (id: number) => {
  await message.delConfirm('确认执行管理解绑吗？')
  await ChannelApi.adminUnbind(id)
  message.success('管理解绑成功')
  await getAdminList()
  await getMyBindings()
}

const syncFromRouteQuery = async () => {
  const tab = String(route.query.tab || '')
  if (tab === 'admin' && hasPermission(['agentx:channel:query'])) {
    activeTab.value = 'admin'
  }
  const userId = Number(route.query.userId)
  if (Number.isFinite(userId) && userId > 0) {
    // 从“用户管理 -> 渠道绑定”进入时，优先只按 userId 查询，避免沿用历史筛选导致看不到数据
    queryParams.pageNo = 1
    queryParams.userId = userId
    queryParams.channelType = undefined
    queryParams.status = undefined
    activeTab.value = 'admin'
  }
  await getAdminList()
}

watch(
  () => [route.query.tab, route.query.userId],
  async () => {
    await syncFromRouteQuery()
  }
)

onActivated(async () => {
  await syncFromRouteQuery()
})

onMounted(async () => {
  await getMyBindings()
  await syncFromRouteQuery()
})
</script>
