import request from '@/config/axios'

export interface AgentxProcessDefinitionVO {
  id: string
  key: string
  name: string
  category?: string
  categoryName?: string
  version: number
  deploymentTime?: string
}

export interface AgentxProcessDefinitionPageReqVO extends PageParam {
  name?: string
  key?: string
  activeOnly?: boolean
}

export interface AgentxProcessDefinitionDetailVO extends AgentxProcessDefinitionVO {
  bpmnXml?: string
}

export const getProcessDefinitionPage = async (params: AgentxProcessDefinitionPageReqVO) => {
  return await request.get({ url: '/agentx/process/definition-page', params })
}

export const getProcessDefinition = async (id: string) => {
  return await request.get({ url: '/agentx/process/definition-get?id=' + id })
}

export const getFallbackProcessDefinitionList = async () => {
  return await request.get({ url: '/bpm/process-definition/simple-list' })
}

export interface AgentxBpmnGenerateReqVO {
  description: string
  processName?: string
  processKey?: string
}

export interface AgentxBpmnGenerateRespVO {
  processName: string
  processKey: string
  bpmnXml: string
  valid: boolean
  message: string
}

export interface AgentxBpmnPreviewReqVO {
  bpmnXml: string
}

export interface AgentxBpmnPreviewRespVO {
  valid: boolean
  message: string
  startEventCount: number
  endEventCount: number
  userTaskCount: number
  gatewayCount: number
}

export const generateAgentxBpmn = async (data: AgentxBpmnGenerateReqVO) => {
  return await request.post<AgentxBpmnGenerateRespVO>({ url: '/agentx/process/bpmn/generate', data })
}

export const previewAgentxBpmn = async (data: AgentxBpmnPreviewReqVO) => {
  return await request.post<AgentxBpmnPreviewRespVO>({ url: '/agentx/process/bpmn/preview', data })
}
