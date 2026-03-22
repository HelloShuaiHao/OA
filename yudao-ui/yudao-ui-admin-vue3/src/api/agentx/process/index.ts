import request from '@/config/axios'

export interface AgentxProcessDefinitionVO {
  id: string
  key: string
  name: string
  category?: string
  categoryName?: string
  version: number
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
