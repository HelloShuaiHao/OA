import request from '@/config/axios'

export interface AgentxChannelConfigVO {
  id?: number
  channelType: 'telegram' | 'wecom' | 'dingtalk'
  channelName: string
  botToken?: string
  agentIds: number[]
  accessControlType: 'all' | 'dept' | 'user'
  deptIds?: number[]
  userIds?: number[]
  agentAccessPolicies?: AgentxChannelAgentAccessPolicyVO[]
  status: number
  createTime?: string
}

export interface AgentxChannelAgentAccessPolicyVO {
  agentId: number
  authMode: 'public' | 'bind_required'
}

export interface AgentxBindGenerateReqVO {
  channelType: string
  channelUserId: string
  channelUsername?: string
}

export interface AgentxChannelAccessEvaluateReqVO {
  channelType: string
  channelUserId: string
  channelUsername?: string
  agentId?: number
  agentKey?: string
}

export interface AgentxChannelAccessEvaluateRespVO {
  accessGranted: boolean
  authRequired: boolean
  bound: boolean
  authMode?: 'public' | 'bind_required'
  accessControlType?: 'all' | 'dept' | 'user'
  bindUrl?: string
  bindToken?: string
  message?: string
}

export interface AgentxUserChannelBindingVO {
  id: number
  userId: number
  channelType: string
  channelUserId: string
  channelUsername?: string
  bindTime: string
  unbindTime?: string
  status: number
  createTime?: string
}

export const getChannelConfigPage = async (params: PageParam & { channelType?: string; channelName?: string; status?: number }) => {
  return await request.get({ url: '/agentx/channel/config/page', params })
}

export const getChannelConfig = async (id: number) => {
  return await request.get({ url: '/agentx/channel/config/get', params: { id } })
}

export const createChannelConfig = async (data: AgentxChannelConfigVO) => {
  return await request.post({ url: '/agentx/channel/config/create', data })
}

export const updateChannelConfig = async (data: AgentxChannelConfigVO) => {
  return await request.put({ url: '/agentx/channel/config/update', data })
}

export const deleteChannelConfig = async (id: number) => {
  return await request.delete({ url: '/agentx/channel/config/delete', params: { id } })
}

export const testChannelConfig = async (data: { channelId?: number; channelType: string; botToken?: string }) => {
  return await request.post({ url: '/agentx/channel/config/test', data })
}

export const evaluateChannelAccess = async (data: AgentxChannelAccessEvaluateReqVO) => {
  return await request.post<AgentxChannelAccessEvaluateRespVO>({ url: '/agentx/channel/access/evaluate', data })
}

export const generateBindLink = async (data: AgentxBindGenerateReqVO) => {
  return await request.post({ url: '/agentx/channel/bind/generate', data })
}

export const confirmBind = async (token: string) => {
  return await request.get({ url: '/agentx/channel/bind/confirm', params: { token } })
}

export const getMyBindings = async () => {
  return await request.get({ url: '/agentx/channel/binding/my' })
}

export const unbind = async (id: number) => {
  return await request.delete({ url: '/agentx/channel/binding/unbind', params: { id } })
}

export const getBindingPage = async (params: PageParam & { userId?: number; channelType?: string; status?: number }) => {
  return await request.get({ url: '/agentx/channel/binding/page', params })
}

export const adminUnbind = async (id: number) => {
  return await request.delete({ url: '/agentx/channel/binding/admin-unbind', params: { id } })
}
