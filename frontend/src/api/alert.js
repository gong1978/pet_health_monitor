import request from './request'

/**
 * 分页查询异常预警
 */
export const getAlerts = (params) => {
  return request.get('/alerts', { params })
}

/**
 * 根据ID查询异常预警详情
 */
export const getAlertById = (alertId) => {
  return request.get(`/alerts/${alertId}`)
}

/**
 * 创建异常预警
 */
export const createAlert = (data) => {
  return request.post('/alerts', data)
}

/**
 * 更新异常预警
 */
export const updateAlert = (alertId, data) => {
  return request.put(`/alerts/${alertId}`, data)
}

/**
 * 删除异常预警
 */
export const deleteAlert = (alertId) => {
  return request.delete(`/alerts/${alertId}`)
}

/**
 * 批量删除异常预警
 */
export const batchDeleteAlerts = (alertIds) => {
  return request.delete('/alerts/batch', { data: alertIds })
}

/**
 * 处理预警（标记为已处理）
 */
export const resolveAlert = (alertId) => {
  return request.post(`/alerts/${alertId}/resolve`)
}

/**
 * 批量处理预警
 */
export const batchResolveAlerts = (alertIds) => {
  return request.post('/alerts/batch-resolve', alertIds)
}
