import request from '@/config/axios'

export interface AgentxTaskProjectionVO {
  id?: number
  scenarioCode: string
  businessKey: string
  idempotencyKey?: string
  openfangTaskRunId?: string
  projectionStatus: number
  riskLevel?: number
  resultSummary?: string
  failureSummary?: string
  auditSummary?: string
  createTime?: string
  updateTime?: string
}

export interface AgentxTaskRuntimeVO {
  taskRunId: string
  workflowId?: string
  workflowVersion?: string
  status?: string
  stage?: string
  pendingApprovalIds?: string[]
  resultSummary?: string
  failureSummary?: string
  auditSummary?: string
  workflowProjections?: Record<string, any>[]
  traceEvents?: Record<string, any>[]
  lastError?: string
}

export const getTaskPage = async (params: PageParam) => {
  return await request.get({ url: '/agentx/task/page', params })
}

export const getTask = async (id: number) => {
  return await request.get({ url: '/agentx/task/get?id=' + id })
}

export const getTaskRuntime = async (taskRunId: string) => {
  return await request.get({ url: '/agentx/task/runtime?taskRunId=' + taskRunId })
}
