import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isAuthenticated = computed(() => !!token.value)

  const setAuth = (accessToken, refreshTokenValue, user) => {
    token.value = accessToken
    refreshToken.value = refreshTokenValue
    userInfo.value = user

    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshTokenValue)
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  const clearAuth = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = {}

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    refreshToken,
    userInfo,
    isAuthenticated,
    setAuth,
    clearAuth,
  }
})