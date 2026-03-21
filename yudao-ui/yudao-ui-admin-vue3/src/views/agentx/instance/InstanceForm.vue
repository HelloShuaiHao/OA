<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="120px"
    >
      <el-form-item label="实例名称" prop="instanceName">
        <el-input v-model="formData.instanceName" placeholder="请输入实例名称" />
      </el-form-item>
      <el-form-item label="实例地址" prop="endpoint">
        <el-input v-model="formData.endpoint" placeholder="例如：http://localhost:4201" />
      </el-form-item>
      <el-form-item label="API Key" prop="apiKey">
        <el-input
          v-model="formData.apiKey"
          :placeholder="formType === 'update' ? '留空表示不修改' : '请输入 API Key'"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="testLoading" @click="handleTestConnection">测试连接</el-button>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as OpenfangInstanceApi from '@/api/agentx/instance'

defineOptions({ name: 'AgentxInstanceForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const testLoading = ref(false)
const formType = ref('')
const formData = ref<OpenfangInstanceApi.OpenfangInstanceVO>({
  id: undefined,
  instanceName: '',
  endpoint: '',
  apiKey: '',
  status: 1
})

const formRules = reactive({
  instanceName: [{ required: true, message: '实例名称不能为空', trigger: 'blur' }],
  endpoint: [{ required: true, message: '实例地址不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})

const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await OpenfangInstanceApi.getInstance(id)
      formData.value.apiKey = ''
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  if (formType.value === 'create' && !formData.value.apiKey) {
    message.warning('新增时 API Key 不能为空')
    return
  }

  formLoading.value = true
  try {
    const data = formData.value as OpenfangInstanceApi.OpenfangInstanceVO
    if (formType.value === 'create') {
      await OpenfangInstanceApi.createInstance(data)
      message.success(t('common.createSuccess'))
    } else {
      await OpenfangInstanceApi.updateInstance(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const handleTestConnection = async () => {
  if (!formData.value.endpoint) {
    message.warning('请先填写实例地址')
    return
  }
  if (!formData.value.id && !formData.value.apiKey) {
    message.warning('请先填写 API Key')
    return
  }

  testLoading.value = true
  try {
    const result = await OpenfangInstanceApi.testConnection({
      id: formData.value.id,
      endpoint: formData.value.endpoint,
      apiKey: formData.value.apiKey
    })
    if (result.online) {
      message.success(`连接成功${result.version ? `，版本 ${result.version}` : ''}`)
    } else {
      message.error(result.message || '连接失败')
    }
  } finally {
    testLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    instanceName: '',
    endpoint: '',
    apiKey: '',
    status: 1
  }
  formRef.value?.resetFields()
}
</script>
