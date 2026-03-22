import request from '@/config/axios'

export interface AgentxTemplateRuleVO {
  field: string
  operator: string
  value: string
  processDefinitionKey?: string
}

export interface AgentxTemplateVO {
  templateType: string
  templateName: string
  description: string
  icon?: string
  defaultCapabilities: string[]
  recommendedProcessKeys: string[]
  defaultRules: AgentxTemplateRuleVO[]
}

export const getTemplateList = async () => {
  return await request.get<AgentxTemplateVO[]>({ url: '/agentx/template/list' })
}

export const getTemplate = async (type: string) => {
  return await request.get<AgentxTemplateVO>({ url: '/agentx/template/get', params: { type } })
}
