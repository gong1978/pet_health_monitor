import request from './request'

/**
 * 分页查询用户
 */
export const pageQueryUsers = (data) => {
  return request.post('/auth/users/page', data)
}

/**
 * 获取用户列表（支持分页和角色筛选）
 */
export const getUsers = (params = {}) => {
  const data = {
    pageNum: params.page || 1,
    pageSize: params.size || 1000,
    role: params.role,
    username: params.username
  }
  return request.post('/auth/users/page', data)
}

/**
 * 获取用户详情
 */
export const getUserDetail = (userId) => {
  return request.get(`/auth/users/${userId}`)
}

/**
 * 创建用户
 */
export const createUser = (data) => {
  return request.post('/auth/users', data)
}

/**
 * 更新用户信息
 */
export const updateUser = (data) => {
  return request.put('/auth/users', data)
}

/**
 * 删除用户
 */
export const deleteUser = (userId) => {
  return request.delete(`/auth/users/${userId}`)
}

/**
 * 批量删除用户
 */
export const batchDeleteUsers = (userIds) => {
  return request.post('/auth/users/batch-delete', userIds)
}
