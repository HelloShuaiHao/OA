<template>
  <ContentWrap>
    <el-page-header :content="isEdit ? '编辑数字员工' : '5 步向导：创建数字员工'" @back="goBack" />
  </ContentWrap>

  <ContentWrap>
    <el-steps :active="activeStep" finish-status="success" simple>
      <el-step title="基本信息" />
      <el-step title="选择模板" />
      <el-step title="配置能力" />
      <el-step title="关联流程" />
      <el-step title="预览发布" />
    </el-steps>
  </ContentWrap>

  <ContentWrap>
    <el-card v-if="activeStep === 0" shadow="never">
      <el-form ref="basicFormRef" :model="formData.basic" :rules="basicRules" label-width="110px">
        <el-form-item label="员工名称" prop="agentName">
          <el-input v-model="formData.basic.agentName" placeholder="例如：请假审批助手" />
        </el-form-item>
        <el-form-item label="员工描述" prop="description">
          <el-input v-model="formData.basic.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="formData.basic.deptId" filterable placeholder="请选择部门" style="width: 320px">
            <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-section">
            <UploadImg
              v-model="formData.basic.avatarUrl"
              :file-size="2"
              width="90px"
              height="90px"
              :show-btn-text="false"
              :show-delete="true"
            />
            <div class="preset-grid">
              <el-avatar
                v-for="item in presetAvatars"
                :key="item"
                :src="item"
                :size="44"
                class="preset-avatar"
                :class="{ active: formData.basic.avatarUrl === item }"
                @click="formData.basic.avatarUrl = item"
              />
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else-if="activeStep === 1" shadow="never">
      <div class="template-grid">
        <div
          v-for="item in templateOptions"
          :key="item.type"
          class="template-card"
          :class="{ active: formData.templateType === item.type }"
          @click="selectTemplate(item.type)"
        >
          <div class="template-title">{{ item.name }}</div>
          <div class="template-desc">{{ item.description }}</div>
        </div>
      </div>
    </el-card>

    <el-card v-else-if="activeStep === 2" shadow="never">
      <el-checkbox-group v-model="formData.capabilityKeys">
        <div v-for="group in capabilityGroups" :key="group.name" class="capability-group">
          <div class="group-title">{{ group.name }}</div>
          <div class="group-options">
            <el-checkbox v-for="item in group.items" :key="item.key" :value="item.key">
              {{ item.name }}
            </el-checkbox>
          </div>
        </div>
      </el-checkbox-group>
    </el-card>

    <StepProcess v-else-if="activeStep === 3" v-model="formData.processConfig" />

    <el-card v-else shadow="never">
      <el-descriptions border :column="1" title="配置预览">
        <el-descriptions-item label="员工名称">{{ formData.basic.agentName }}</el-descriptions-item>
        <el-descriptions-item label="员工描述">{{ formData.basic.description }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ currentDeptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="头像">
          <el-avatar :src="formData.basic.avatarUrl" :size="42">{{ formData.basic.agentName?.slice(0, 1) }}</el-avatar>
        </el-descriptions-item>
        <el-descriptions-item label="模板类型">
          {{ templateOptions.find((t) => t.type === formData.templateType)?.name || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="能力数量">{{ formData.capabilityKeys.length }}</el-descriptions-item>
        <el-descriptions-item label="流程选择策略">
          {{ formData.processConfig.selectionMode === 'auto' ? 'AI 自动选择' : '规则选择' }}
        </el-descriptions-item>
        <el-descriptions-item label="已关联流程">
          {{ formData.processConfig.selectedProcesses.map((i) => i.name).join('、') || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </ContentWrap>

  <ContentWrap>
    <div class="footer-actions">
      <el-button @click="goBack">取消</el-button>
      <el-button :disabled="activeStep === 0 || submitLoading" @click="prevStep">上一步</el-button>
      <el-button v-if="activeStep < 4" type="primary" :disabled="submitLoading" @click="nextStep">下一步</el-button>
      <el-button v-if="activeStep === 4 && isEdit" type="primary" :loading="submitLoading" @click="submitUpdate">
        保存修改
      </el-button>
      <el-button v-else-if="activeStep === 4" type="primary" :loading="submitLoading" @click="submitDraft">保存草稿</el-button>
      <el-button v-if="activeStep === 4 && !isEdit" type="success" :loading="submitLoading" @click="submitActivate">
        激活并发布
      </el-button>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import * as DeptApi from '@/api/system/dept'
import * as AgentApi from '@/api/agentx/agent'
import type { AgentxProcessDefinitionVO } from '@/api/agentx/process'
import * as TemplateApi from '@/api/agentx/template'
import { UploadImg } from '@/components/UploadFile'
import StepProcess from './components/StepProcess.vue'

defineOptions({ name: 'AgentxAgentCreate' })

interface SelectionRule {
  field: string
  operator: string
  value: string
  processDefinitionId: string
}

const message = useMessage()
const route = useRoute()
const router = useRouter()
const activeStep = ref(0)
const submitLoading = ref(false)
const basicFormRef = ref()
const deptOptions = ref<DeptApi.DeptVO[]>([])
const isEdit = computed(() => !!route.query.id)

const fallbackTemplateOptions = [
  { type: 'leave', name: '请假审批助手', description: '处理请假流程与审批路由' },
  { type: 'expense', name: '报销审批助手', description: '处理报销单据与审核' },
  { type: 'procurement', name: '采购审批助手', description: '处理采购申请与预算校验' },
  { type: 'crm', name: '客户跟进助手', description: '跟进客户并推动转化流程' },
  { type: 'analysis', name: '数据分析助手', description: '进行数据查询与分析汇总' },
  { type: 'doc', name: '文档处理助手', description: '解析、归档与文档流转' },
  { type: 'custom', name: '自定义', description: '完全自定义能力与流程' }
]
const templateOptions = ref(fallbackTemplateOptions)

const capabilityGroups = [
  {
    name: '审批管理',
    items: [
      { key: 'approve_task', name: '审批任务处理' },
      { key: 'query_task', name: '待办任务查询' },
      { key: 'workflow_route', name: '流程路由决策' },
      { key: 'approval_record', name: '审批记录检索' }
    ]
  },
  {
    name: '数据查询',
    items: [
      { key: 'query_user', name: '组织人员查询' },
      { key: 'query_form', name: '业务单据查询' },
      { key: 'query_report', name: '报表数据查询' }
    ]
  },
  {
    name: '通知能力',
    items: [
      { key: 'notify_im', name: 'IM 消息通知' },
      { key: 'notify_mail', name: '邮件通知' },
      { key: 'notify_sms', name: '短信通知' }
    ]
  }
]

const templateCapabilityMap: Record<string, string[]> = {
  leave: ['approve_task', 'query_task', 'workflow_route', 'approval_record'],
  expense: ['approve_task', 'query_task', 'workflow_route', 'approval_record'],
  procurement: ['approve_task', 'query_task', 'workflow_route', 'query_form'],
  crm: ['query_user', 'query_form', 'notify_im'],
  analysis: ['query_report', 'query_form', 'notify_mail'],
  doc: ['query_form', 'notify_im'],
  custom: []
}

const presetAvatars = [
  'https://api.dicebear.com/7.x/bottts/svg?seed=leave',
  'https://api.dicebear.com/7.x/bottts/svg?seed=expense',
  'https://api.dicebear.com/7.x/bottts/svg?seed=procurement',
  'https://api.dicebear.com/7.x/bottts/svg?seed=crm',
  'https://api.dicebear.com/7.x/bottts/svg?seed=analysis',
  'https://api.dicebear.com/7.x/bottts/svg?seed=doc',
  'https://api.dicebear.com/7.x/bottts/svg?seed=service',
  'https://api.dicebear.com/7.x/bottts/svg?seed=robot',
  'https://api.dicebear.com/7.x/bottts/svg?seed=agentx',
  'https://api.dicebear.com/7.x/bottts/svg?seed=oa'
]

const formData = reactive({
  basic: {
    agentName: '',
    description: '',
    deptId: undefined as undefined | number,
    avatarUrl: ''
  },
  templateType: '',
  capabilityKeys: [] as string[],
  processConfig: {
    selectedProcesses: [] as AgentxProcessDefinitionVO[],
    selectionMode: 'rule' as 'rule' | 'auto',
    rules: [] as SelectionRule[]
  }
})

const basicRules = reactive({
  agentName: [
    { required: true, message: '请输入员工名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度为 2-50 字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入员工描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度为 10-500 字符', trigger: 'blur' }
  ],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }]
})

const currentDeptName = computed(() => {
  return deptOptions.value.find((item) => item.id === formData.basic.deptId)?.name
})

const selectTemplate = (type: string) => {
  formData.templateType = type
  formData.capabilityKeys = templateCapabilityMap[type] ? [...templateCapabilityMap[type]] : []
}

const loadTemplateOptions = async () => {
  try {
    const list = await TemplateApi.getTemplateList()
    if (!list || list.length === 0) {
      templateOptions.value = fallbackTemplateOptions
      return
    }
    templateOptions.value = list.map((item) => ({
      type: item.templateType,
      name: item.templateName,
      description: item.description
    }))
    list.forEach((item) => {
      templateCapabilityMap[item.templateType] = item.defaultCapabilities || []
    })
  } catch {
    templateOptions.value = fallbackTemplateOptions
  }
}

const goBack = () => {
  router.push('/agentx/agent')
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value -= 1
  }
}

const nextStep = async () => {
  if (activeStep.value === 0) {
    const valid = await basicFormRef.value?.validate()
    if (!valid) return
  }
  if (activeStep.value === 1 && !formData.templateType) {
    message.warning('请选择模板')
    return
  }
  if (activeStep.value === 2 && formData.capabilityKeys.length === 0) {
    message.warning('至少勾选 1 个能力')
    return
  }
  if (activeStep.value === 3) {
    const selected = formData.processConfig.selectedProcesses.length > 0
    if (!selected) {
      message.warning('至少关联 1 个流程')
      return
    }
    if (formData.processConfig.selectionMode === 'rule') {
      if (formData.processConfig.selectedProcesses.length > 1 && formData.processConfig.rules.length === 0) {
        message.warning('多流程下规则模式至少配置 1 条规则')
        return
      }
      const validRule = formData.processConfig.rules.every(
        (rule) => !!rule.field && !!rule.operator && !!rule.value && !!rule.processDefinitionId
      )
      if (!validRule) {
        message.warning('请完善规则配置')
        return
      }
    }
  }
  if (activeStep.value < 4) {
    activeStep.value += 1
  }
}

const buildPayload = (): AgentApi.AgentVO => {
  const capabilityMap = Object.fromEntries(
    capabilityGroups.flatMap((group) => group.items.map((item) => [item.key, item.name]))
  )
  return {
    id: isEdit.value ? Number(route.query.id) : undefined,
    agentName: formData.basic.agentName,
    description: formData.basic.description,
    deptId: formData.basic.deptId as number,
    avatarUrl: formData.basic.avatarUrl,
    templateType: formData.templateType,
    capabilities: formData.capabilityKeys.map((key) => ({
      capabilityKey: key,
      capabilityName: capabilityMap[key] || key,
      enabled: true
    })),
    processes: formData.processConfig.selectedProcesses.map((item) => ({
      processDefinitionId: item.id,
      processDefinitionKey: item.key,
      processName: item.name,
      processVersion: item.version
    })),
    selectionMode: formData.processConfig.selectionMode,
    selectionRules: formData.processConfig.rules
  }
}

const submitDraft = async () => {
  submitLoading.value = true
  try {
    await AgentApi.createAgentDraft(buildPayload())
    message.success('草稿保存成功')
    router.push('/agentx/agent')
  } finally {
    submitLoading.value = false
  }
}

const submitActivate = async () => {
  submitLoading.value = true
  try {
    const id = await AgentApi.createAgentPublish(buildPayload())
    message.success('数字员工已激活')
    router.push(`/agentx/agent/detail?id=${id}`)
  } finally {
    submitLoading.value = false
  }
}

const submitUpdate = async () => {
  submitLoading.value = true
  try {
    await AgentApi.updateAgent(buildPayload())
    message.success('修改成功')
    router.push(`/agentx/agent/detail?id=${route.query.id}`)
  } finally {
    submitLoading.value = false
  }
}

const loadEditData = async () => {
  if (!isEdit.value) return
  const data = await AgentApi.getAgent(Number(route.query.id))
  formData.basic.agentName = data.agentName
  formData.basic.description = data.description
  formData.basic.deptId = data.deptId
  formData.basic.avatarUrl = data.avatarUrl || ''
  formData.templateType = data.templateType || 'custom'
  formData.capabilityKeys = (data.capabilities || []).map((item) => item.capabilityKey || '')
  formData.processConfig.selectedProcesses = (data.processes || []).map((item) => ({
    id: item.processDefinitionId,
    key: item.processDefinitionKey || '',
    name: item.processName || item.processDefinitionKey || item.processDefinitionId,
    version: item.processVersion || 1
  }))
  formData.processConfig.selectionMode = (data.selectionMode as 'rule' | 'auto') || 'rule'
  formData.processConfig.rules = (data.selectionRules || []).map((rule) => ({
    field: rule.field,
    operator: rule.operator,
    value: rule.value,
    processDefinitionId: rule.processDefinitionId
  }))
}

onMounted(async () => {
  deptOptions.value = await DeptApi.getSimpleDeptList()
  await loadTemplateOptions()
  await loadEditData()
})
</script>

<style scoped>
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.template-card {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: var(--el-color-primary-light-5);
}

.template-card.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.template-title {
  font-weight: 600;
}

.template-desc {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.capability-group + .capability-group {
  margin-top: 12px;
}

.group-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.group-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.preset-grid {
  display: grid;
  grid-template-columns: repeat(5, 44px);
  gap: 8px;
}

.preset-avatar {
  cursor: pointer;
  border: 2px solid transparent;
}

.preset-avatar.active {
  border-color: var(--el-color-primary);
}
</style>
