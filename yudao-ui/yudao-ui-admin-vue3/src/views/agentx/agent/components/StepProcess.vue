<template>
  <div class="step-process">
    <el-card class="process-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>从流程库选择</span>
          <div class="header-actions">
            <el-input
              v-model="queryParams.name"
              placeholder="按流程名称搜索"
              clearable
              style="width: 220px"
              @keyup.enter="getList"
            />
            <el-button @click="getList"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="process-list">
        <el-empty v-if="!list.length" description="暂无可选流程" :image-size="80" />
        <div
          v-for="item in list"
          :key="item.id"
          class="process-row"
          :class="{ active: isSelected(item.id) }"
          @click="handleRowClick(item)"
        >
          <el-checkbox
            :model-value="isSelected(item.id)"
            @click.stop="toggleProcessSelection(item, !isSelected(item.id))"
          />
          <div class="process-row__main">
            <div class="process-row__title">{{ item.name }}</div>
            <div class="process-row__meta">
              <span>Key: {{ item.key }}</span>
              <span>版本: {{ item.version }}</span>
              <span>分类: {{ item.categoryName || '-' }}</span>
              <span>创建时间: {{ item.deploymentTime ? formatDateTime(item.deploymentTime) : '-' }}</span>
            </div>
          </div>
          <el-button link type="primary" @click.stop="openPreview(item)">查看流程图</el-button>
        </div>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <el-card class="strategy-card" shadow="never">
      <template #header>
        <span>流程选择策略</span>
      </template>

      <el-radio-group v-model="selectionMode">
        <el-radio value="rule">规则选择</el-radio>
        <el-radio value="auto">AI 自动选择</el-radio>
      </el-radio-group>

      <div v-if="selectionMode === 'rule'" class="rules-panel">
        <el-button size="small" @click="addRule">添加规则</el-button>
        <el-empty v-if="rules.length === 0" description="暂无规则，请添加" :image-size="72" />

        <div v-for="(rule, index) in rules" :key="index" class="rule-item">
          <el-select v-model="rule.field" placeholder="字段" style="width: 160px">
            <el-option label="用户角色" value="userRole" />
            <el-option label="请假天数" value="leaveDays" />
            <el-option label="金额" value="amount" />
          </el-select>

          <el-select v-model="rule.operator" placeholder="操作符" style="width: 120px">
            <el-option label="等于" value="=" />
            <el-option label="大于" value=">" />
            <el-option label="小于" value="<" />
          </el-select>

          <el-input v-model="rule.value" placeholder="值" style="width: 180px" />

          <span class="arrow">→</span>

          <el-select
            v-model="rule.processDefinitionId"
            placeholder="选择目标流程"
            style="width: 280px"
          >
            <el-option
              v-for="item in selectedProcesses"
              :key="item.id"
              :label="`${item.name} (v${item.version})`"
              :value="item.id"
            />
          </el-select>

          <el-button type="danger" link @click="deleteRule(index)">删除</el-button>
        </div>
      </div>
    </el-card>

    <el-alert
      type="info"
      show-icon
      :closable="false"
      class="summary-alert"
      :title="`已选流程 ${selectedProcesses.length} 个`"
      :description="selectedProcesses.map((i) => i.name).join('、') || '尚未选择流程'"
    />

    <el-dialog v-model="previewVisible" title="流程图预览" width="85%" destroy-on-close>
      <el-empty v-if="!previewModel.bpmnXml" description="流程图数据为空" />
      <div v-else class="preview-wrapper">
        <MyProcessViewer :xml="previewModel.bpmnXml" :view="previewModel" class="process-viewer" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { MyProcessViewer } from '@/components/bpmnProcessDesigner/package'
import * as ProcessApi from '@/api/agentx/process'

interface SelectionRule {
  field: string
  operator: string
  value: string
  processDefinitionId: string
}

interface StepProcessModel {
  selectedProcesses: ProcessApi.AgentxProcessDefinitionVO[]
  selectionMode: 'rule' | 'auto'
  rules: SelectionRule[]
}

const props = withDefaults(
  defineProps<{
    modelValue?: StepProcessModel
  }>(),
  {
    modelValue: () => ({
      selectedProcesses: [],
      selectionMode: 'rule',
      rules: []
    })
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: StepProcessModel): void
}>()

const loading = ref(false)
const list = ref<ProcessApi.AgentxProcessDefinitionVO[]>([])
const total = ref(0)
const fallbackList = ref<ProcessApi.AgentxProcessDefinitionVO[]>([])
const usingFallback = ref(false)

const queryParams = reactive<ProcessApi.AgentxProcessDefinitionPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: '',
  key: '',
  activeOnly: true
})

const selectedProcessIds = ref<string[]>([])
const selectedProcessMap = ref<Record<string, ProcessApi.AgentxProcessDefinitionVO>>({})
const selectionMode = ref<'rule' | 'auto'>(props.modelValue.selectionMode || 'rule')
const rules = ref<SelectionRule[]>(props.modelValue.rules || [])

const selectedProcesses = computed(() =>
  selectedProcessIds.value
    .map((id) => selectedProcessMap.value[id])
    .filter((item): item is ProcessApi.AgentxProcessDefinitionVO => !!item)
)

const syncModelValue = () => {
  emit('update:modelValue', {
    selectedProcesses: selectedProcesses.value,
    selectionMode: selectionMode.value,
    rules: rules.value
  })
}

watch(
  () => props.modelValue,
  (value) => {
    const nextMode = value?.selectionMode || 'rule'
    if (nextMode !== selectionMode.value) {
      selectionMode.value = nextMode
    }

    const nextRules = value?.rules || []
    if (JSON.stringify(nextRules) !== JSON.stringify(rules.value)) {
      // deep clone array to avoid reactive proxies cross-contamination
      rules.value = JSON.parse(JSON.stringify(nextRules))
    }

    const nextIds = (value?.selectedProcesses || []).map((item) => item.id)
    if (JSON.stringify(nextIds) !== JSON.stringify(selectedProcessIds.value)) {
      selectedProcessIds.value = nextIds
      selectedProcessMap.value = Object.fromEntries(
        (value?.selectedProcesses || []).map((item) => [item.id, { ...item }])
      )
    }
  },
  { deep: true }
)

watch([selectedProcesses, selectionMode, rules], syncModelValue, { deep: true })

const getList = async () => {
  loading.value = true
  try {
    const data = await ProcessApi.getProcessDefinitionPage(queryParams)
    if (!data.list || data.list.length === 0) {
      await loadFallbackList()
    } else {
      usingFallback.value = false
      list.value = data.list || []
      total.value = data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const loadFallbackList = async () => {
  const rows = await ProcessApi.getFallbackProcessDefinitionList()
  usingFallback.value = true
  fallbackList.value = (rows || []).map((item) => ({
    id: item.id,
    key: item.key,
    name: item.name,
    version: item.version || 1,
    category: item.category,
    categoryName: item.categoryName,
    deploymentTime: item.deploymentTime
  }))
  const filtered = fallbackList.value.filter((item) => {
    const matchName = !queryParams.name || item.name?.includes(queryParams.name)
    const matchKey = !queryParams.key || item.key?.includes(queryParams.key)
    return matchName && matchKey
  })
  total.value = filtered.length
  const start = (queryParams.pageNo - 1) * queryParams.pageSize
  list.value = filtered.slice(start, start + queryParams.pageSize)
}

const resetQuery = () => {
  queryParams.pageNo = 1
  queryParams.name = ''
  queryParams.key = ''
  getList()
}

const pruneInvalidRules = () => {
  for (const rule of rules.value) {
    if (!selectedProcessMap.value[rule.processDefinitionId]) {
      rule.processDefinitionId = ''
    }
  }
}

const isSelected = (id: string) => {
  return selectedProcessIds.value.includes(id)
}

const toggleProcessSelection = (row: ProcessApi.AgentxProcessDefinitionVO, checked: boolean | string | number) => {
  if (checked) {
    selectedProcessMap.value[row.id] = row
    if (!selectedProcessIds.value.includes(row.id)) {
      selectedProcessIds.value = [...selectedProcessIds.value, row.id]
    }
  } else {
    delete selectedProcessMap.value[row.id]
    selectedProcessIds.value = selectedProcessIds.value.filter((id) => id !== row.id)
  }
  pruneInvalidRules()
  syncModelValue()
}

const handleRowClick = (row: ProcessApi.AgentxProcessDefinitionVO) => {
  toggleProcessSelection(row, !isSelected(row.id))
}

const addRule = () => {
  rules.value.push({
    field: '',
    operator: '=',
    value: '',
    processDefinitionId: selectedProcesses.value[0]?.id || ''
  })
  syncModelValue()
}

const deleteRule = (index: number) => {
  rules.value.splice(index, 1)
  syncModelValue()
}

const previewVisible = ref(false)
const previewModel = ref<{ bpmnXml: string }>({ bpmnXml: '' })
const openPreview = async (row: ProcessApi.AgentxProcessDefinitionVO) => {
  const detail = await ProcessApi.getProcessDefinition(row.id)
  if (!detail?.bpmnXml) {
    ElMessage.warning(usingFallback.value ? '当前环境暂未提供流程图预览，请先完成流程选择' : '流程图数据为空')
    return
  }
  previewModel.value = {
    bpmnXml: detail?.bpmnXml || ''
  }
  previewVisible.value = true
}

onMounted(() => {
  selectedProcessIds.value = (props.modelValue?.selectedProcesses || []).map((item) => item.id)
  selectedProcessMap.value = Object.fromEntries(
    (props.modelValue?.selectedProcesses || []).map((item) => [item.id, item])
  )
  getList()
})
</script>

<style scoped>
.step-process {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.process-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.process-row:hover {
  border-color: var(--el-color-primary-light-5);
}

.process-row.active {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
  background: var(--el-color-primary-light-9);
}

.process-row__main {
  flex: 1;
  min-width: 0;
}

.process-row__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.process-row__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.rules-panel {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.arrow {
  color: var(--el-text-color-secondary);
}

.summary-alert {
  margin-top: -4px;
}

.preview-wrapper {
  height: 72vh;
  min-height: 460px;
}

.process-viewer {
  height: 100%;
}

@media (max-width: 1200px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
