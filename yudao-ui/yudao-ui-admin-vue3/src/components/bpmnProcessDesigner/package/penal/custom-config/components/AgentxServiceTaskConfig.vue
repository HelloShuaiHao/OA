<template>
  <div class="panel-tab__content">
    <el-alert
      title="AgentX 扩展节点配置"
      type="info"
      :closable="false"
      description="按节点类型填写参数后保存。参数会写入 BPMN 扩展属性，供后端 Delegate 执行。"
      class="mb-3"
    />
    <el-form label-width="120px">
      <el-form-item label="节点类型">
        <el-select v-model="form.nodeType" class="w-full" @change="handleTypeChange">
          <el-option
            v-for="item in nodeTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Delegate">
        <el-input v-model="form.delegateExpression" readonly />
      </el-form-item>

      <template v-if="form.nodeType === 'AI_DECISION'">
        <el-form-item label="Prompt 模板">
          <el-input v-model="form.aiPromptTemplate" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="输入变量">
          <el-input v-model="form.aiInputVariables" placeholder="leaveDays,amount" />
        </el-form-item>
        <el-form-item label="超时(秒)">
          <el-input v-model="form.aiTimeoutSeconds" />
        </el-form-item>
      </template>

      <template v-if="form.nodeType === 'TOOL_CALL'">
        <el-form-item label="Tool 名称">
          <el-input v-model="form.toolName" placeholder="bpm_query_tasks" />
        </el-form-item>
        <el-form-item label="参数(JSON)">
          <el-input v-model="form.toolParams" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="重试次数">
          <el-input v-model="form.toolMaxRetries" />
        </el-form-item>
      </template>

      <template v-if="form.nodeType === 'DATA_QUERY'">
        <el-form-item label="查询模式">
          <el-select v-model="form.queryMode" class="w-full">
            <el-option label="SQL" value="sql" />
            <el-option label="API" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="SQL / URL">
          <el-input v-model="form.querySql" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="参数">
          <el-input v-model="form.queryArgs" placeholder='[1, "A"]' />
        </el-form-item>
      </template>

      <template v-if="form.nodeType === 'EXTERNAL_API'">
        <el-form-item label="API URL">
          <el-input v-model="form.apiUrl" />
        </el-form-item>
        <el-form-item label="HTTP 方法">
          <el-select v-model="form.httpMethod" class="w-full">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证类型">
          <el-select v-model="form.authType" class="w-full">
            <el-option label="NONE" value="NONE" />
            <el-option label="BEARER" value="BEARER" />
            <el-option label="BASIC" value="BASIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="Token">
          <el-input v-model="form.authToken" />
        </el-form-item>
        <el-form-item label="重试 / 超时">
          <el-input v-model="form.retryTimes" placeholder="retryTimes" />
          <el-input v-model="form.timeoutSeconds" placeholder="timeoutSeconds" class="ml-2" />
        </el-form-item>
      </template>

      <el-form-item>
        <el-button type="primary" @click="saveConfig">保存节点配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

defineOptions({ name: 'AgentxServiceTaskConfig' })

const props = defineProps({
  id: String,
  type: String,
  businessObject: {
    type: Object,
    default: () => ({})
  }
})

const prefix = inject('prefix') as string
const bpmnInstances = () => (window as any)?.bpmnInstances

const nodeTypeOptions = [
  { label: 'AI 决策节点', value: 'AI_DECISION', delegateExpression: '${agentxAiDecisionDelegate}' },
  { label: 'Tool 调用节点', value: 'TOOL_CALL', delegateExpression: '${agentxToolCallDelegate}' },
  { label: '数据查询节点', value: 'DATA_QUERY', delegateExpression: '${agentxDataQueryDelegate}' },
  { label: '外部 API 节点', value: 'EXTERNAL_API', delegateExpression: '${agentxExternalApiDelegate}' }
]

const form = reactive<Record<string, string>>({
  nodeType: 'AI_DECISION',
  delegateExpression: '${agentxAiDecisionDelegate}',
  aiPromptTemplate: '',
  aiInputVariables: '',
  aiTimeoutSeconds: '30',
  toolName: '',
  toolParams: '',
  toolMaxRetries: '2',
  queryMode: 'sql',
  querySql: '',
  queryArgs: '',
  apiUrl: '',
  httpMethod: 'GET',
  authType: 'NONE',
  authToken: '',
  retryTimes: '2',
  timeoutSeconds: '30'
})

const handleTypeChange = (value: string) => {
  const option = nodeTypeOptions.find((item) => item.value === value)
  if (option) {
    form.delegateExpression = option.delegateExpression
  }
}

const toPropertyMap = (businessObject: any) => {
  const result: Record<string, string> = {}
  const values = businessObject?.extensionElements?.values || []
  const propertyGroup = values.find((item) => item.$type === `${prefix}:Properties`)
  ;(propertyGroup?.values || []).forEach((item) => {
    if (item?.name) {
      result[item.name] = item.value || ''
    }
  })
  return result
}

const resetForm = () => {
  const element = bpmnInstances()?.bpmnElement
  const bo = element?.businessObject || props.businessObject
  if (!bo) return
  const propertyMap = toPropertyMap(bo)
  form.nodeType = propertyMap.agentxNodeType || form.nodeType
  handleTypeChange(form.nodeType)
  form.aiPromptTemplate = propertyMap.aiPromptTemplate || ''
  form.aiInputVariables = propertyMap.aiInputVariables || ''
  form.aiTimeoutSeconds = propertyMap.aiTimeoutSeconds || '30'
  form.toolName = propertyMap.toolName || ''
  form.toolParams = propertyMap.toolParams || ''
  form.toolMaxRetries = propertyMap.toolMaxRetries || '2'
  form.queryMode = propertyMap.queryMode || 'sql'
  form.querySql = propertyMap.querySql || propertyMap.apiUrl || ''
  form.queryArgs = propertyMap.queryArgs || ''
  form.apiUrl = propertyMap.apiUrl || ''
  form.httpMethod = propertyMap.httpMethod || 'GET'
  form.authType = propertyMap.authType || 'NONE'
  form.authToken = propertyMap.authToken || ''
  form.retryTimes = propertyMap.retryTimes || '2'
  form.timeoutSeconds = propertyMap.timeoutSeconds || '30'
}

const saveConfig = () => {
  const error = validateForm()
  if (error) {
    ElMessage.warning(error)
    return
  }
  const instances = bpmnInstances()
  if (!instances || !instances.bpmnElement) return
  const element = instances.bpmnElement
  const bo = element.businessObject

  const values = bo?.extensionElements?.values || []
  const otherExtensions = values.filter((item) => item.$type !== `${prefix}:Properties`)
  const properties = Object.entries(form)
    .filter(([key, value]) => key !== 'delegateExpression' && value !== '')
    .map(([name, value]) => instances.moddle.create(`${prefix}:Property`, { name, value }))
  const propertyGroup = instances.moddle.create(`${prefix}:Properties`, { values: properties })
  const extensionElements = instances.moddle.create('bpmn:ExtensionElements', {
    values: [...otherExtensions, propertyGroup]
  })

  instances.modeling.updateProperties(element, {
    extensionElements,
    [`${prefix}:delegateExpression`]: form.delegateExpression
  })
  ElMessage.success('节点配置已保存')
}

const validateForm = (): string => {
  if (form.nodeType === 'AI_DECISION') {
    if (!form.aiPromptTemplate.trim()) return 'AI 决策节点必须填写 Prompt 模板'
    if (!isPositiveNumber(form.aiTimeoutSeconds)) return 'AI 决策超时必须是正整数（秒）'
    return ''
  }
  if (form.nodeType === 'TOOL_CALL') {
    if (!form.toolName.trim()) return 'Tool 调用节点必须填写 Tool 名称'
    if (form.toolParams.trim() && !isJsonLike(form.toolParams)) return 'Tool 参数必须是合法 JSON'
    if (!isNonNegativeNumber(form.toolMaxRetries)) return 'Tool 重试次数必须是非负整数'
    return ''
  }
  if (form.nodeType === 'DATA_QUERY') {
    if (!form.querySql.trim()) return '数据查询节点必须填写 SQL 或 API URL'
    if (form.queryMode === 'sql' && !/^select\s+/i.test(form.querySql.trim())) {
      return 'SQL 模式仅允许 SELECT 语句'
    }
    if (form.queryArgs.trim() && !isJsonLike(form.queryArgs)) return '查询参数必须是合法 JSON'
    return ''
  }
  if (form.nodeType === 'EXTERNAL_API') {
    if (!form.apiUrl.trim()) return '外部 API 节点必须填写 API URL'
    if (!isNonNegativeNumber(form.retryTimes)) return '外部 API 重试次数必须是非负整数'
    if (!isPositiveNumber(form.timeoutSeconds)) return '外部 API 超时必须是正整数（秒）'
    if (form.authType !== 'NONE' && !form.authToken.trim()) return '启用认证时必须填写 Token'
  }
  return ''
}

const isJsonLike = (text: string): boolean => {
  try {
    JSON.parse(text)
    return true
  } catch {
    return false
  }
}

const isPositiveNumber = (value: string): boolean => /^\d+$/.test(value) && Number(value) > 0

const isNonNegativeNumber = (value: string): boolean => /^\d+$/.test(value)

watch(
  () => props.businessObject,
  () => resetForm(),
  { immediate: true, deep: true }
)
</script>

<style scoped lang="scss">
.w-full {
  width: 100%;
}
.ml-2 {
  margin-left: 8px;
}
.mb-3 {
  margin-bottom: 12px;
}
</style>
