import request from './request'

/**
 * 用户登录
 */
export const login = (data) => {
  return request.post('/auth/login', data)
}

/**
 * 用户注册
 */
export const register = (data) => {
  return request.post('/auth/register', data)
}

/**
 * 刷新 Token
 */
export const refreshToken = (refreshTokenValue) => {
  return request.post('/auth/refresh-token', {}, {
    headers: {
      Authorization: `Bearer ${refreshTokenValue}`,
    },
  })
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return request.get('/auth/user-info')
}

/**
 * 用户登出
 */
export const logout = () => {
  return request.post('/auth/logout')
}

