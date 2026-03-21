import request from '@/config/axios'

export interface AgentxAuditEventVO {
  id?: number
  tenantId?: number
  initiatorId?: string
  agentCode?: string
  eventType: string
  scenarioCode?: string
  businessKey?: string
  openfangTaskRunId?: string
  openfangApprovalId?: string
  toolName?: string
  riskLevel?: number
  dataScopeSummary?: string
  requestSummary?: string
  durationMs?: number
  errorCode?: number
  businessImpactSummary?: string
  resultSummary?: string
  createTime?: string
  updateTime?: string
}

export const getAuditPage = async (params: PageParam) => {
  return await request.get({ url: '/agentx/audit/page', params })
}

export const getAudit = async (id: number) => {
  return await request.get({ url: '/agentx/audit/get?id=' + id })
}
