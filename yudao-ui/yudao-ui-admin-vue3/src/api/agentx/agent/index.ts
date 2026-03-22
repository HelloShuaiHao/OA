import request from '@/config/axios'

export interface AgentCapabilityItemVO {
  capabilityKey?: string
  capabilityName: string
  enabled?: boolean
  maxCallsPerHour?: number
  conditions?: string
}

export interface AgentProcessItemVO {
  processDefinitionId: string
  processDefinitionKey?: string
  processName?: string
  processVersion?: number
  priority?: number
}

export interface AgentSelectionRuleItemVO {
  field: string
  operator: string
  value: string
  processDefinitionId: string
  processDefinitionKey?: string
}

export interface AgentVO {
  id?: number
  agentName: string
  agentKey?: string
  description: string
  avatarUrl?: string
  deptId: number
  deptName?: string
  status?: number
  templateType?: string
  capabilities?: AgentCapabilityItemVO[]
  processes?: AgentProcessItemVO[]
  selectionMode?: 'rule' | 'auto'
  selectionRules?: AgentSelectionRuleItemVO[]
  createTime?: string
  updateTime?: string
}

export interface AgentPageReqVO extends PageParam {
  agentName?: string
  deptId?: number
  status?: number
  templateType?: string
}

export const getAgentPage = async (params: AgentPageReqVO) => {
  return await request.get({ url: '/agentx/agent/page', params })
}

export const getAgent = async (id: number) => {
  return await request.get({ url: '/agentx/agent/get?id=' + id })
}

export const checkAgentName = async (agentName: string, id?: number) => {
  return await request.get({ url: '/agentx/agent/check-name', params: { agentName, id } })
}

export const createAgentDraft = async (data: AgentVO) => {
  return await request.post({ url: '/agentx/agent/create-draft', data })
}

export const createAgentPublish = async (data: AgentVO) => {
  return await request.post({ url: '/agentx/agent/create-publish', data })
}

export const updateAgent = async (data: AgentVO) => {
  return await request.put({ url: '/agentx/agent/update', data })
}

export const deleteAgent = async (id: number) => {
  return await request.delete({ url: `/agentx/agent/${id}` })
}

export const updateAgentStatus = async (id: number, status: number) => {
  return await request.put({ url: `/agentx/agent/${id}/status`, data: { status } })
}
