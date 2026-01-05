import request from './request'

/**
 * 分页查询健康报告
 */
export const getHealthReports = (params) => {
  return request.get('/health-reports', { params })
}

/**
 * 根据ID查询健康报告详情
 */
export const getHealthReportById = (reportId) => {
  return request.get(`/health-reports/${reportId}`)
}

/**
 * 创建健康报告
 */
export const createHealthReport = (data) => {
  return request.post('/health-reports', data)
}

/**
 * 更新健康报告
 */
export const updateHealthReport = (reportId, data) => {
  return request.put(`/health-reports/${reportId}`, data)
}

/**
 * 删除健康报告
 */
export const deleteHealthReport = (reportId) => {
  return request.delete(`/health-reports/${reportId}`)
}

/**
 * 批量删除健康报告
 */
export const batchDeleteHealthReports = (reportIds) => {
  return request.delete('/health-reports/batch', { data: reportIds })
}
