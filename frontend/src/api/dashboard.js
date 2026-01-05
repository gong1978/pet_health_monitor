import request from './request'

/**
 * 获取首页统计数据
 */
export const getDashboardStats = () => {
  return request.get('/dashboard/stats')
}
