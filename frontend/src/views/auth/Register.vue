<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>🐾 创建账号</h1>
        <p>加入宠物健康监测社区</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="fullName">
          <el-input
            v-model="registerForm.fullName"
            placeholder="请输入真实姓名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select
            v-model="registerForm.role"
            placeholder="请选择身份"
            clearable
          >
            <el-option label="宠物主人" :value="3" />
<!--            <el-option label="兽医" :value="2" />-->
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="register-btn"
            @click="handleRegister"
            :loading="loading"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>已有账号？<router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  fullName: '',
  email: '',
  phone: '',
  role: 3,
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (value === '') {
    callback(new Error('请输入邮箱'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (value === '') {
    callback(new Error('请输入手机号'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' },
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' },
  ],
  role: [
    { required: true, message: '请选择身份', trigger: 'change' },
  ],
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await register({
          username: registerForm.username,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
          fullName: registerForm.fullName,
          email: registerForm.email,
          phone: registerForm.phone,
          role: registerForm.role,
        })

        if (response.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        }
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
  padding: 20px 0;
}

.register-box {
  width: 100%;
  max-width: 450px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-header {
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

.register-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.register-footer {
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

