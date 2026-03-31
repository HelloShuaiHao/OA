<template>
  <!-- BPMN设计器 -->
  <template v-if="modelData.type === BpmModelType.BPMN">
    <div class="ai-generate-panel">
      <el-input
        v-model="aiDescription"
        type="textarea"
        :rows="2"
        maxlength="2000"
        show-word-limit
        placeholder="AI 生成流程草案：输入流程描述，例如 员工提交请假申请，主管审批，通过后人事备案，拒绝则结束"
      />
      <div class="ai-generate-panel__actions">
        <el-button type="primary" :loading="aiGenerating" @click="handleAiGenerateDraft">
          AI 生成流程草案
        </el-button>
      </div>
    </div>
    <BpmModelEditor
      v-if="showDesigner"
      :model-id="modelData.id"
      :model-key="modelData.key"
      :model-name="modelData.name"
      @success="handleDesignSuccess"
    />
  </template>

  <!-- Simple设计器 -->
  <template v-else>
    <SimpleModelDesign
      v-if="showDesigner"
      :model-name="modelData.name"
      :model-form-id="modelData.formId"
      :model-form-type="modelData.formType"
      :start-user-ids="modelData.startUserIds"
      :start-dept-ids="modelData.startDeptIds"
      @success="handleDesignSuccess"
    />
  </template>
</template>

<script lang="ts" setup>
import { BpmModelType } from '@/utils/constants'
import BpmModelEditor from './editor/index.vue'
import SimpleModelDesign from '../../simple/SimpleModelDesign.vue'
import { generateAgentxBpmn, previewAgentxBpmn } from '@/api/agentx/process'

// 创建本地数据副本
const modelData = defineModel<any>()

const processData = inject('processData') as Ref
const message = useMessage()
const aiDescription = ref('')
const aiGenerating = ref(false)

/** 表单校验 */
const validate = async () => {
  try {
    // 获取最新的流程数据
    if (!processData.value) {
      throw new Error('请设计流程')
    }
    return true
  } catch (error) {
    throw error
  }
}
/** 处理设计器保存成功 */
const handleDesignSuccess = async (data?: any) => {
  if (data) {
    // 创建新的对象以触发响应式更新
    const newModelData = {
      ...modelData.value,
      bpmnXml: modelData.value.type === BpmModelType.BPMN ? data : null,
      simpleModel: modelData.value.type === BpmModelType.BPMN ? null : data
    }
    // 使用emit更新父组件的数据
    await nextTick()
    //更新表单的模型数据部分
    modelData.value = newModelData
  }
}

/** AI 生成 BPMN 草案并注入设计器 */
const handleAiGenerateDraft = async () => {
  const description = aiDescription.value.trim()
  if (!description) {
    message.warning('请先输入流程描述')
    return
  }
  if (description.length < 20) {
    message.warning('流程描述至少输入 20 个字，方便 AI 生成完整流程')
    return
  }
  aiGenerating.value = true
  try {
    const generated = await generateAgentxBpmn({
      description,
      processName: modelData.value?.name,
      processKey: modelData.value?.key
    })
    const preview = await previewAgentxBpmn({ bpmnXml: generated.bpmnXml })
    if (!preview.valid) {
      message.warning(`生成完成但校验未通过：${preview.message}`)
      return
    }
    processData.value = generated.bpmnXml
    modelData.value = {
      ...modelData.value,
      bpmnXml: generated.bpmnXml
    }
    message.success('AI 流程草案已生成并注入设计器')
  } finally {
    aiGenerating.value = false
  }
}

/** 是否显示设计器 */
const showDesigner = computed(() => {
  return Boolean(modelData.value?.key && modelData.value?.name)
})
defineExpose({
  validate
})
</script>

<style scoped>
.ai-generate-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
  padding: 12px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.ai-generate-panel__actions {
  display: flex;
  justify-content: flex-end;
}
</style>
