<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">个人信息</span>
        </div>
      </template>

      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="rules"
        label-width="100px"
        v-loading="loading"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h4>基本信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份">
                <el-input v-model="profileForm.roleName" disabled />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="fullName">
                <el-input 
                  v-model="profileForm.fullName" 
                  placeholder="请输入真实姓名"
                  clearable 
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input 
                  v-model="profileForm.phone" 
                  placeholder="请输入手机号"
                  clearable 
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input 
                  v-model="profileForm.email" 
                  type="email"
                  placeholder="请输入邮箱"
                  clearable 
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="注册时间">
                <el-input v-model="profileForm.createdAt" disabled />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 密码修改 -->
        <div class="form-section">
          <h4>修改密码（可选）</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input 
                  v-model="profileForm.currentPassword" 
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                  clearable 
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="profileForm.newPassword" 
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                  clearable 
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input 
                  v-model="profileForm.confirmPassword" 
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                  clearable 
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button 
            type="primary" 
            @click="handleSubmit"
            :loading="submitting"
          >
            保存修改
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/profile'

const profileFormRef = ref()
const loading = ref(false)
const submitting = ref(false)

const profileForm = reactive({
  userId: '',
  username: '',
  fullName: '',
  email: '',
  phone: '',
  role: '',
  roleName: '',
  createdAt: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 原始数据备份，用于重置
const originalData = ref({})

// 验证规则
const validatePassword = (rule, value, callback) => {
  if (value && value.length < 6) {
    callback(new Error('新密码长度不能少于6个字符'))
  } else {
    if (profileForm.confirmPassword !== '') {
      profileFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (profileForm.newPassword && !value) {
    callback(new Error('请确认新密码'))
  } else if (value && value !== profileForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateCurrentPassword = (rule, value, callback) => {
  if (profileForm.newPassword && !value) {
    callback(new Error('修改密码时必须提供当前密码'))
  } else {
    callback()
  }
}

const rules = {
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  currentPassword: [
    { validator: validateCurrentPassword, trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
}

// 获取个人信息
const fetchProfile = async () => {
  loading.value = true
  try {
    const response = await getProfile()
    if (response.code === 200) {
      const data = response.data
      Object.assign(profileForm, {
        userId: data.userId,
        username: data.username,
        fullName: data.fullName || '',
        email: data.email || '',
        phone: data.phone || '',
        role: data.role,
        roleName: data.roleName,
        createdAt: data.createdAt,
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      })
      
      // 保存原始数据
      originalData.value = {
        fullName: data.fullName || '',
        email: data.email || '',
        phone: data.phone || '',
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '获取个人信息失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = {
          fullName: profileForm.fullName,
          email: profileForm.email,
          phone: profileForm.phone,
        }

        // 如果要修改密码
        if (profileForm.newPassword) {
          submitData.currentPassword = profileForm.currentPassword
          submitData.newPassword = profileForm.newPassword
          submitData.confirmPassword = profileForm.confirmPassword
        }

        const response = await updateProfile(submitData)
        if (response.code === 200) {
          ElMessage.success('个人信息更新成功')
          
          // 清空密码字段
          profileForm.currentPassword = ''
          profileForm.newPassword = ''
          profileForm.confirmPassword = ''
          
          // 重新获取数据
          await fetchProfile()
        }
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  profileForm.fullName = originalData.value.fullName
  profileForm.email = originalData.value.email
  profileForm.phone = originalData.value.phone
  profileForm.currentPassword = ''
  profileForm.newPassword = ''
  profileForm.confirmPassword = ''
  
  // 清除验证结果
  if (profileFormRef.value) {
    profileFormRef.value.clearValidate()
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
  }
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
    margin-bottom: 20px;
  }

  h4 {
    margin: 0 0 20px 0;
    font-size: 16px;
    color: #66a6ff;
    font-weight: 600;
    border-left: 4px solid #66a6ff;
    padding-left: 10px;
  }
}

.form-actions {
  text-align: center;
  margin-top: 30px;
  
  .el-button {
    min-width: 120px;
    margin: 0 10px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }

  .el-col {
    margin-bottom: 10px;
  }
}
</style>
