<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="130px"
    >
      <el-form-item label="场景编码" prop="scenarioCode">
        <el-input v-model="formData.scenarioCode" placeholder="例如：oa.leave.approval" />
      </el-form-item>
      <el-form-item label="场景名称" prop="scenarioName">
        <el-input v-model="formData.scenarioName" placeholder="请输入场景名称" />
      </el-form-item>
      <el-form-item label="Workflow ID" prop="openfangWorkflowId">
        <el-input v-model="formData.openfangWorkflowId" placeholder="例如：leave-approval-assistant" />
      </el-form-item>
      <el-form-item label="Workflow 版本" prop="workflowVersion">
        <el-input v-model="formData.workflowVersion" placeholder="例如：1.0.0" />
      </el-form-item>
      <el-form-item label="启用状态" prop="enabled">
        <el-radio-group v-model="formData.enabled">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="配置 JSON" prop="config">
        <el-input
          v-model="formData.config"
          type="textarea"
          :rows="8"
          placeholder='例如：{"autoApproveMaxDays":2,"contextProviders":[...]}'
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as ScenarioApi from '@/api/agentx/scenario'

defineOptions({ name: 'AgentxScenarioForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<ScenarioApi.ScenarioConfigVO>({
  id: undefined,
  scenarioCode: '',
  scenarioName: '',
  openfangWorkflowId: '',
  workflowVersion: '',
  enabled: 1,
  config: ''
})

const formRules = reactive({
  scenarioCode: [{ required: true, message: '场景编码不能为空', trigger: 'blur' }],
  scenarioName: [{ required: true, message: '场景名称不能为空', trigger: 'blur' }],
  openfangWorkflowId: [{ required: true, message: 'Workflow ID 不能为空', trigger: 'blur' }],
  enabled: [{ required: true, message: '启用状态不能为空', trigger: 'change' }]
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
      formData.value = await ScenarioApi.getScenario(id)
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

  formLoading.value = true
  try {
    const data = formData.value as ScenarioApi.ScenarioConfigVO
    if (formType.value === 'create') {
      await ScenarioApi.createScenario(data)
      message.success(t('common.createSuccess'))
    } else {
      await ScenarioApi.updateScenario(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    scenarioCode: '',
    scenarioName: '',
    openfangWorkflowId: '',
    workflowVersion: '',
    enabled: 1,
    config: ''
  }
  formRef.value?.resetFields()
}
</script>
