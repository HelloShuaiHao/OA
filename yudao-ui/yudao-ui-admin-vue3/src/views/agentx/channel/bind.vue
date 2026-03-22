<template>
  <ContentWrap v-loading="loading">
    <el-result :icon="resultType" :title="title" :sub-title="subTitle">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as ChannelApi from '@/api/agentx/channel'
import { getAccessToken } from '@/utils/auth'

defineOptions({ name: 'AgentxChannelBindPage' })

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const resultType = ref<'success' | 'error' | 'warning' | 'info'>('info')
const title = ref('正在处理绑定...')
const subTitle = ref('请稍候')

const goHome = () => {
  router.push('/')
}

const handleBind = async () => {
  const token = String(route.query.token || '')
  if (!token) {
    resultType.value = 'error'
    title.value = '绑定失败'
    subTitle.value = '缺少绑定 token'
    return
  }
  if (!getAccessToken()) {
    router.replace(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  loading.value = true
  try {
    const data = await ChannelApi.confirmBind(token)
    resultType.value = data.bound ? 'success' : 'warning'
    title.value = data.bound ? '绑定成功' : '绑定状态未知'
    subTitle.value = data.message || ''
  } catch (error: any) {
    resultType.value = 'error'
    title.value = '绑定失败'
    subTitle.value = error?.message || '绑定链接已过期或无效'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  handleBind()
})
</script>
