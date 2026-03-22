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

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="list"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" reserve-selection />
        <el-table-column label="流程名称" prop="name" min-width="220" />
        <el-table-column label="流程 Key" prop="key" min-width="180" />
        <el-table-column label="版本" prop="version" width="80" align="center" />
        <el-table-column label="分类" prop="categoryName" min-width="120" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openPreview(scope.row)">查看流程图</el-button>
          </template>
        </el-table-column>
      </el-table>

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
const tableRef = ref()
const list = ref<ProcessApi.AgentxProcessDefinitionVO[]>([])
const total = ref(0)

const queryParams = reactive<ProcessApi.AgentxProcessDefinitionPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: '',
  key: '',
  activeOnly: true
})

const selectedProcessMap = ref<Record<string, ProcessApi.AgentxProcessDefinitionVO>>({})
const selectionMode = ref<'rule' | 'auto'>(props.modelValue.selectionMode || 'rule')
const rules = ref<SelectionRule[]>(props.modelValue.rules || [])

const selectedProcesses = computed(() => Object.values(selectedProcessMap.value))

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
    rules.value = value?.rules || []
    selectedProcessMap.value = Object.fromEntries(
      (value?.selectedProcesses || []).map((item) => [item.id, item])
    )
  },
  { deep: true }
)

watch([selectedProcesses, selectionMode, rules], syncModelValue, { deep: true })

const getList = async () => {
  loading.value = true
  try {
    const data = await ProcessApi.getProcessDefinitionPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
    nextTick(() => {
      for (const row of list.value) {
        tableRef.value?.toggleRowSelection(row, !!selectedProcessMap.value[row.id])
      }
    })
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.pageNo = 1
  queryParams.name = ''
  queryParams.key = ''
  getList()
}

const handleSelectionChange = (rows: ProcessApi.AgentxProcessDefinitionVO[]) => {
  const currentPageIds = list.value.map((item) => item.id)
  for (const id of currentPageIds) {
    delete selectedProcessMap.value[id]
  }
  for (const row of rows) {
    selectedProcessMap.value[row.id] = row
  }

  for (const rule of rules.value) {
    if (!selectedProcessMap.value[rule.processDefinitionId]) {
      rule.processDefinitionId = ''
    }
  }
}

const addRule = () => {
  rules.value.push({
    field: '',
    operator: '=',
    value: '',
    processDefinitionId: selectedProcesses.value[0]?.id || ''
  })
}

const deleteRule = (index: number) => {
  rules.value.splice(index, 1)
}

const previewVisible = ref(false)
const previewModel = ref<{ bpmnXml: string }>({ bpmnXml: '' })
const openPreview = async (row: ProcessApi.AgentxProcessDefinitionVO) => {
  const detail = await ProcessApi.getProcessDefinition(row.id)
  previewModel.value = {
    bpmnXml: detail?.bpmnXml || ''
  }
  previewVisible.value = true
}

onMounted(() => {
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
