import request from '@/config/axios'

export interface AgentxApprovalBindingVO {
  id?: number
  scenarioCode: string
  businessKey: string
  openfangTaskRunId?: string
  openfangApprovalId?: string
  bpmProcessInstanceId?: string
  riskLevel?: number
  decisionStatus?: number
  callbackRetryCount?: number
  callbackFailed?: boolean
  callbackLastError?: string
  actionSummary?: string
  createTime?: string
  updateTime?: string
}

export const getApprovalPage = async (params: PageParam) => {
  return await request.get({ url: '/agentx/approval/page', params })
}

export const getApproval = async (id: number) => {
  return await request.get({ url: '/agentx/approval/get?id=' + id })
}
