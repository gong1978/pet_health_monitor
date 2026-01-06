<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>🐾 宠物健康监测系统</h1>
        <p>智能宠物健康与行为监测平台</p>
      </div>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              class="login-btn"
              @click="handleLogin"
              :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login({
          username: loginForm.username,
          password: loginForm.password,
        })

        if (response.code === 200) {
          const { accessToken, refreshToken, ...userInfo } = response.data
          authStore.setAuth(accessToken, refreshToken, userInfo)
          ElMessage.success('登录成功')
          router.push('/dashboard')
        }
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  h1 {
    font-size: 28px;
    margin-bottom: 10px;
    color: #333;
  }

  p {
    font-size: 14px;
    color: #999;
  }
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;

  p {
    color: #666;
  }

  a {
    color: #66a6ff;
    text-decoration: none;

    &:hover {
      color: #4a90e2;
    }
  }
}
</style>