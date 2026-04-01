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
          <el-input
            v-model="formData.basic.agentName"
            placeholder="例如：请假审批助手"
            @blur="handleAgentNameBlur"
          />
        </el-form-item>
        <el-form-item label="员工描述" prop="description">
          <el-input v-model="formData.basic.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="formData.basic.deptId" placeholder="请选择部门" style="width: 320px">
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
          <div class="template-icon">
            <Icon :icon="item.icon" />
          </div>
          <div class="template-title">{{ item.name }}</div>
          <div class="template-desc">{{ item.description }}</div>
        </div>
      </div>
    </el-card>

    <el-card v-else-if="activeStep === 2" shadow="never">
      <template v-if="isCustomTemplate">
        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="自定义员工支持先用自然语言描述职责，再按需补能力和流程。"
          description="如果你现在还没有确定流程，可以先写清楚这个员工该做什么、什么情况下介入、回答风格和边界。保存草稿时不强制选流程。"
          class="mb-16px"
        />
        <el-form label-width="120px">
          <el-form-item label="工作方式说明">
            <el-input
              v-model="formData.customSpec"
              type="textarea"
              :rows="6"
              placeholder="例如：负责根据员工描述判断是否需要发起 OA 流程；能回答制度问题；涉及请假/外出/报销时再选择对应流程，不确定时先追问关键信息。"
            />
          </el-form-item>
        </el-form>
        <div class="group-title">可选能力</div>
        <div class="custom-capability-tip">这一步不是必填；如果你已经知道它需要哪些能力，可以先勾上。</div>
        <el-checkbox-group v-model="formData.capabilityKeys">
          <div v-for="group in capabilityGroups" :key="group.name" class="capability-group">
            <div class="group-title">{{ group.name }}</div>
            <div class="group-options">
              <el-checkbox v-for="item in group.items" :key="item.key" :value="item.key" class="capability-option">
                <div class="capability-option__body">
                  <div class="capability-option__name">{{ item.name }}</div>
                  <div class="capability-option__desc">{{ item.description }}</div>
                  <el-tag size="small" type="info" effect="plain">可启用</el-tag>
                </div>
              </el-checkbox>
            </div>
          </div>
        </el-checkbox-group>
      </template>
      <el-checkbox-group v-else v-model="formData.capabilityKeys">
        <div v-for="group in capabilityGroups" :key="group.name" class="capability-group">
          <div class="group-title">{{ group.name }}</div>
          <div class="group-options">
            <el-checkbox v-for="item in group.items" :key="item.key" :value="item.key" class="capability-option">
              <div class="capability-option__body">
                <div class="capability-option__name">{{ item.name }}</div>
                <div class="capability-option__desc">{{ item.description }}</div>
                <el-tag size="small" type="info" effect="plain">可启用</el-tag>
              </div>
            </el-checkbox>
          </div>
        </div>
      </el-checkbox-group>
    </el-card>

    <template v-else-if="activeStep === 3">
      <ContentWrap v-if="isCustomTemplate">
        <el-alert
          type="warning"
          show-icon
          :closable="false"
          title="自定义员工可以先不选流程保存草稿。"
          description="只有在你准备“激活并发布”时，才建议至少关联 1 个流程，这样它在需要 OA 交互时才知道该跟随哪条流程。"
        />
      </ContentWrap>
      <StepProcess
        :model-value="formData.processConfig"
        @update:model-value="handleProcessConfigChange"
      />
    </template>

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
        <el-descriptions-item v-if="isCustomTemplate" label="自定义说明">
          {{ formData.customSpec || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="能力数量">{{ formData.capabilityKeys.length }}</el-descriptions-item>

        <el-descriptions-item label="已关联流程">
          {{ formData.processConfig.selectedProcesses.map((i) => i.name).join('、') || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </ContentWrap>

  <ContentWrap>
    <div class="footer-actions">
      <span v-if="activeStep === 4 && !isEdit" class="publish-tip">
        保存草稿仅保存到 OA；点击“激活并发布”后才会同步到 OpenFang 并可用于渠道接入。
      </span>
      <el-button @click="goBack">取消</el-button>
      <el-button :disabled="activeStep === 0 || submitLoading" @click="prevStep">上一步</el-button>
      <el-button v-if="activeStep < 4" type="primary" :disabled="nextStepDisabled" @click="nextStep">下一步</el-button>
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



interface TemplateOption {
  type: string
  name: string
  description: string
  icon: string
}

const message = useMessage()
const route = useRoute()
const router = useRouter()
const activeStep = ref(0)
const submitLoading = ref(false)
const basicFormRef = ref()
const deptOptions = ref<DeptApi.DeptVO[]>([])
const isEdit = computed(() => !!route.query.id)
const checkingName = ref(false)
const lastCheckedAgentName = ref('')
const lastCheckedAvailable = ref<boolean | null>(null)

const templateMetaMap: Record<string, { name: string; description: string; icon: string }> = {
  leave: { name: '请假审批助手', description: '处理请假流程与审批路由', icon: 'ep:calendar' },
  expense: { name: '报销审批助手', description: '处理报销单据与审核', icon: 'ep:wallet' },
  procurement: { name: '采购审批助手', description: '处理采购申请与预算校验', icon: 'ep:shopping-cart-full' },
  crm: { name: '客户跟进助手', description: '跟进客户并推动转化流程', icon: 'ep:user-filled' },
  analysis: { name: '数据分析助手', description: '进行数据查询与分析汇总', icon: 'ep:data-analysis' },
  doc: { name: '文档处理助手', description: '解析、归档与文档流转', icon: 'ep:document' },
  custom: { name: '自定义', description: '完全自定义能力与流程', icon: 'ep:setting' }
}

const fallbackTemplateOptions: TemplateOption[] = Object.entries(templateMetaMap).map(([type, meta]) => ({
  type,
  ...meta
}))
const templateOptions = ref<TemplateOption[]>(fallbackTemplateOptions)
const CUSTOM_SPEC_MARKER = '\n\n【自定义工作方式】\n'

const capabilityGroups = [
  {
    name: '审批管理',
    items: [
      { key: 'approve_task', name: '审批任务处理', description: '代办审批、通过驳回与批注处理。' },
      { key: 'query_task', name: '待办任务查询', description: '查询当前用户或代理人的待办审批任务。' },
      { key: 'workflow_route', name: '流程路由决策', description: '根据业务条件选择最合适的审批流程。' },
      { key: 'approval_record', name: '审批记录检索', description: '查看历史审批意见、轨迹和处理结果。' }
    ]
  },
  {
    name: '数据查询',
    items: [
      { key: 'query_user', name: '组织人员查询', description: '查询部门、岗位和人员基础信息。' },
      { key: 'query_form', name: '业务单据查询', description: '读取请假、报销、采购等业务单据。' },
      { key: 'query_report', name: '报表数据查询', description: '查询报表指标并输出汇总结果。' }
    ]
  },
  {
    name: '通知能力',
    items: [
      { key: 'notify_im', name: 'IM 消息通知', description: '通过站内信或 IM 推送任务状态提醒。' },
      { key: 'notify_mail', name: '邮件通知', description: '通过邮件发送审批结果与待办提醒。' },
      { key: 'notify_sms', name: '短信通知', description: '通过短信触达紧急审批与异常告警。' }
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
  customSpec: '',
  capabilityKeys: [] as string[],
  processConfig: {
    selectedProcesses: [] as AgentxProcessDefinitionVO[]
  }
})
const isCustomTemplate = computed(() => formData.templateType === 'custom')

const checkAgentNameRemote = async (agentName: string) => {
  if (lastCheckedAgentName.value === agentName && lastCheckedAvailable.value !== null) {
    return lastCheckedAvailable.value
  }
  checkingName.value = true
  try {
    const available = await AgentApi.checkAgentName(agentName, isEdit.value ? Number(route.query.id) : undefined)
    lastCheckedAgentName.value = agentName
    lastCheckedAvailable.value = available
    return available
  } finally {
    checkingName.value = false
  }
}

const handleAgentNameBlur = async () => {
  const agentName = formData.basic.agentName.trim()
  if (!agentName || agentName.length < 2 || agentName.length > 50) {
    return
  }
  await checkAgentNameRemote(agentName)
}

const basicRules = reactive({
  agentName: [
    { required: true, message: '请输入员工名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度为 2-50 字符', trigger: 'blur' },
    {
      async validator(_rule, value, callback) {
        const agentName = value?.trim()
        if (!agentName || agentName.length < 2 || agentName.length > 50) {
          callback()
          return
        }
        try {
          const available = await checkAgentNameRemote(agentName)
          if (!available) {
            callback(new Error('名称已存在'))
            return
          }
          callback()
        } catch {
          callback(new Error('名称校验失败，请稍后重试'))
        }
      },
      trigger: 'blur'
    }
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

const isBasicInfoComplete = computed(() => {
  return (
    formData.basic.agentName.trim().length >= 2 &&
    formData.basic.agentName.trim().length <= 50 &&
    formData.basic.description.trim().length >= 10 &&
    formData.basic.description.trim().length <= 500 &&
    !!formData.basic.deptId
  )
})

const isProcessStepComplete = computed(() => {
  return formData.processConfig.selectedProcesses.length > 0
})

const nextStepDisabled = computed(() => {
  if (submitLoading.value) {
    return true
  }
  if (checkingName.value) {
    return true
  }
  if (activeStep.value === 0) {
    return !isBasicInfoComplete.value
  }
  if (activeStep.value === 1) {
    return !formData.templateType
  }
  if (activeStep.value === 2) {
    if (isCustomTemplate.value) {
      return false
    }
    return formData.capabilityKeys.length === 0
  }
  if (activeStep.value === 3) {
    return false
  }
  return false
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
      name: item.templateName || templateMetaMap[item.templateType]?.name || item.templateType,
      description: item.description || templateMetaMap[item.templateType]?.description || '使用该模板快速创建数字员工',
      icon: templateMetaMap[item.templateType]?.icon || templateMetaMap.custom.icon
    }))
    list.forEach((item) => {
      templateCapabilityMap[item.templateType] = item.defaultCapabilities || []
    })
  } catch {
    templateOptions.value = fallbackTemplateOptions
  }
}

const goBack = () => {
  router.push('/agentx/agent/list')
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
  if (activeStep.value === 2 && !isCustomTemplate.value && formData.capabilityKeys.length === 0) {
    message.warning('至少勾选 1 个能力')
    return
  }
  if (activeStep.value === 3) {
    const selected = formData.processConfig.selectedProcesses.length > 0
    if (!selected && !isCustomTemplate.value) {
      message.warning('至少关联 1 个流程')
      return
    }
  }
  if (activeStep.value < 4) {
    activeStep.value += 1
  }
}

const handleProcessConfigChange = (value: {
  selectedProcesses: AgentxProcessDefinitionVO[]
}) => {
  formData.processConfig.selectedProcesses = value.selectedProcesses || []
}

const buildPayload = (): AgentApi.AgentVO => {
  const capabilityMap = Object.fromEntries(
    capabilityGroups.flatMap((group) => group.items.map((item) => [item.key, item.name]))
  )
  const normalizedDescription = formData.basic.description.trim()
  const normalizedCustomSpec = formData.customSpec.trim()
  const mergedDescription =
    isCustomTemplate.value && normalizedCustomSpec
      ? `${normalizedDescription}${CUSTOM_SPEC_MARKER}${normalizedCustomSpec}`
      : normalizedDescription
  const capabilities =
    isCustomTemplate.value && formData.capabilityKeys.length === 0 && normalizedCustomSpec
      ? [
          {
            capabilityKey: 'custom_brief',
            capabilityName: '自定义职责说明',
            enabled: true,
            conditions: normalizedCustomSpec
          }
        ]
      : formData.capabilityKeys.map((key) => ({
          capabilityKey: key,
          capabilityName: capabilityMap[key] || key,
          enabled: true
        }))
  return {
    id: isEdit.value ? Number(route.query.id) : undefined,
    agentName: formData.basic.agentName,
    description: mergedDescription,
    deptId: formData.basic.deptId as number,
    avatarUrl: formData.basic.avatarUrl,
    templateType: formData.templateType,
    capabilities,
    processes: formData.processConfig.selectedProcesses.map((item) => ({
      processDefinitionId: item.id,
      processDefinitionKey: item.key,
      processName: item.name,
      processVersion: item.version
    })),
    selectionMode: 'auto',
    selectionRules: []
  }
}

const sleep = (ms: number) => new Promise((resolve) => window.setTimeout(resolve, ms))

const waitForAgentReady = async (id: number, options?: { requireSync?: boolean }) => {
  const expectedProcessCount = formData.processConfig.selectedProcesses.length
  const expectedCapabilityCount = formData.capabilityKeys.length

  for (let attempt = 0; attempt < 12; attempt++) {
    const detail = await AgentApi.getAgent(id)
    const processReady = (detail.processes?.length || 0) >= expectedProcessCount
    const capabilityReady = (detail.capabilities?.length || 0) >= expectedCapabilityCount
    const syncReady = !options?.requireSync || detail.lastSyncStatus === 1 || detail.lastSyncStatus === 2
    const statusReady = !options?.requireSync || detail.status === 1
    if (processReady && capabilityReady && syncReady && statusReady) {
      return
    }
    await sleep(300)
  }
}

const submitDraft = async () => {
  submitLoading.value = true
  try {
    const id = await AgentApi.createAgentDraft(buildPayload())
    await waitForAgentReady(id)
    message.success('草稿保存成功')
    router.push(`/agentx/agent/detail/${id}`)
  } finally {
    submitLoading.value = false
  }
}

const submitActivate = async () => {
  if (isCustomTemplate.value && formData.processConfig.selectedProcesses.length === 0) {
    message.warning('自定义员工激活前至少关联 1 个流程；如果还没确定，可以先保存草稿')
    return
  }
  submitLoading.value = true
  try {
    const id = await AgentApi.createAgentPublish(buildPayload())
    await waitForAgentReady(id, { requireSync: true })
    message.success('数字员工已激活')
    router.push(`/agentx/agent/detail/${id}`)
  } finally {
    submitLoading.value = false
  }
}

const submitUpdate = async () => {
  submitLoading.value = true
  try {
    await AgentApi.updateAgent(buildPayload())
    message.success('修改成功')
    router.push(`/agentx/agent/detail/${route.query.id}`)
  } finally {
    submitLoading.value = false
  }
}

const loadEditData = async () => {
  if (!isEdit.value) return
  const data = await AgentApi.getAgent(Number(route.query.id))
  const description = data.description || ''
  const markerIndex = description.indexOf(CUSTOM_SPEC_MARKER)
  formData.basic.agentName = data.agentName
  formData.basic.description = markerIndex >= 0 ? description.slice(0, markerIndex) : description
  formData.customSpec = markerIndex >= 0 ? description.slice(markerIndex + CUSTOM_SPEC_MARKER.length) : ''
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
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.publish-tip {
  margin-right: auto;
  color: var(--el-text-color-secondary);
  font-size: 12px;
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

.template-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 10px;
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

.custom-capability-tip {
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.group-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.capability-option {
  margin-right: 0;
  align-items: flex-start;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  padding: 12px;
}

.capability-option :deep(.el-checkbox__label) {
  padding-left: 10px;
}

.capability-option__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.capability-option__name {
  font-weight: 600;
}

.capability-option__desc {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
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
