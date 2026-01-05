import request from './request'

/**
 * 分页查询行为识别
 */
export const getBehaviorAnalysis = (params) => {
  return request.get('/behavior-analysis', { params })
}

/**
 * 根据ID查询行为识别详情
 */
export const getBehaviorAnalysisById = (behaviorId) => {
  return request.get(`/behavior-analysis/${behaviorId}`)
}

/**
 * 创建行为识别
 */
export const createBehaviorAnalysis = (data) => {
  return request.post('/behavior-analysis', data)
}

/**
 * 更新行为识别
 */
export const updateBehaviorAnalysis = (behaviorId, data) => {
  return request.put(`/behavior-analysis/${behaviorId}`, data)
}

/**
 * 删除行为识别
 */
export const deleteBehaviorAnalysis = (behaviorId) => {
  return request.delete(`/behavior-analysis/${behaviorId}`)
}

/**
 * 批量删除行为识别
 */
export const batchDeleteBehaviorAnalysis = (behaviorIds) => {
  return request.delete('/behavior-analysis/batch', { data: behaviorIds })
}
