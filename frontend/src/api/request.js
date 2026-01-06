import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
    baseURL: '/api',
    timeout: 10000,
})

service.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore()
        if (authStore.token) {
            config.headers.Authorization = `Bearer ${authStore.token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

service.interceptors.response.use(
    (response) => {
        const res = response.data

        // 1. 成功
        if (res.code === 200) {
            return res
        }
        // 2. Token 过期
        else if (res.code === 401) {
            const authStore = useAuthStore()
            authStore.clearAuth()
            router.push('/login')
            ElMessage.error(res.message || res.msg || '登录已过期')
            return Promise.reject(new Error('Unauthorized'))
        }
        // 3. 其他业务错误
        else {
            // 【核心修复】如果后端没传 message，给一个默认值，防止显示 "null" 或 "操作失败"
            // 很多时候后端抛出 RuntimeException 但没写 message，前端就会收到 null
            const errorMsg = res.message || res.msg || `请求处理失败 (Code: ${res.code})`

            // 避免重复弹窗（可选）
            ElMessage.error(errorMsg)
            return Promise.reject(new Error(errorMsg))
        }
    },
    (error) => {
        // 4. 网络层面的错误 (404, 500 等)
        let message = '网络连接异常'
        if (error.response && error.response.data) {
            // 尝试获取后端返回的详细错误
            message = error.response.data.message || error.response.data.msg || message
        }
        ElMessage.error(message)
        return Promise.reject(error)
    }
)

export default service