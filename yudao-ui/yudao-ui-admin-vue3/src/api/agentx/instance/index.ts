import request from '@/config/axios'

export interface OpenfangInstanceVO {
  id?: number
  tenantId?: number
  instanceName: string
  endpoint: string
  apiKey?: string
  status: number
  version?: string
  lastHeartbeat?: string
  createTime?: string
}

export interface OpenfangInstanceTestReqVO {
  id?: number
  endpoint?: string
  apiKey?: string
}

export interface OpenfangInstanceTestRespVO {
  online: boolean
  version?: string
  message?: string
  checkedAt?: string
}

// 查询 OpenFang 实例分页
export const getInstancePage = async (params: PageParam) => {
  return await request.get({ url: '/agentx/instance/page', params })
}

// 查询 OpenFang 实例详情
export const getInstance = async (id: number) => {
  return await request.get({ url: '/agentx/instance/get?id=' + id })
}

// 创建 OpenFang 实例
export const createInstance = async (data: OpenfangInstanceVO) => {
  return await request.post({ url: '/agentx/instance/create', data })
}

// 更新 OpenFang 实例
export const updateInstance = async (data: OpenfangInstanceVO) => {
  return await request.put({ url: '/agentx/instance/update', data })
}

// 删除 OpenFang 实例
export const deleteInstance = async (id: number) => {
  return await request.delete({ url: '/agentx/instance/delete?id=' + id })
}

// 测试连接
export const testConnection = async (data: OpenfangInstanceTestReqVO) => {
  return await request.post({ url: '/agentx/instance/test-connection', data })
}
