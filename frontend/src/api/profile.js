import request from './request'

/**
 * 获取个人信息
 */
export const getProfile = () => {
  return request.get('/auth/profile')
}

/**
 * 更新个人信息
 */
export const updateProfile = (data) => {
  return request.put('/auth/profile', data)
}
