import request from '@/config/axios'

export interface ScenarioConfigVO {
  id?: number
  scenarioCode: string
  scenarioName: string
  openfangWorkflowId: string
  workflowVersion?: string
  enabled: number
  config?: string
  createTime?: string
}

// 查询场景配置分页
export const getScenarioPage = async (params: PageParam) => {
  return await request.get({ url: '/agentx/scenario/page', params })
}

// 查询场景配置详情
export const getScenario = async (id: number) => {
  return await request.get({ url: '/agentx/scenario/get?id=' + id })
}

// 新增场景配置
export const createScenario = async (data: ScenarioConfigVO) => {
  return await request.post({ url: '/agentx/scenario/create', data })
}

// 修改场景配置
export const updateScenario = async (data: ScenarioConfigVO) => {
  return await request.put({ url: '/agentx/scenario/update', data })
}

// 删除场景配置
export const deleteScenario = async (id: number) => {
  return await request.delete({ url: '/agentx/scenario/delete?id=' + id })
}
