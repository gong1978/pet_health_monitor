import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      // Token 过期或无效
      const authStore = useAuthStore()
      authStore.clearAuth()
      router.push('/login')
      ElMessage.error(res.message || '登录已过期，请重新登录')
      return Promise.reject(new Error(res.message))
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service

