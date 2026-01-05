<template>
  <div class="user-management-container">
    <el-card class="box-card">
      <!-- 搜索条件 -->
      <div class="search-section">
        <el-form :model="searchForm" label-width="100px" class="search-form">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="用户名">
                <el-input
                  v-model="searchForm.username"
                  placeholder="请输入用户名"
                  clearable
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="真实姓名">
                <el-input
                  v-model="searchForm.fullName"
                  placeholder="请输入真实姓名"
                  clearable
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="邮箱">
                <el-input
                  v-model="searchForm.email"
                  placeholder="请输入邮箱"
                  clearable
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="角色">
                <el-select
                  v-model="searchForm.role"
                  placeholder="请选择角色"
                  clearable
                >
                  <el-option label="管理员" :value="1" />
                  <el-option label="兽医" :value="2" />
                  <el-option label="宠物主人" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="手机号">
                <el-input
                  v-model="searchForm.phone"
                  placeholder="请输入手机号"
                  clearable
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="18">
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 操作按钮 -->
      <div class="operation-section">
        <el-button type="primary" @click="handleCreate">新增用户</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedUsers.length === 0" class="ml-10">
          批量删除 ({{ selectedUsers.length }})
        </el-button>
      </div>

      <!-- 用户表格 -->
      <el-table
        :data="tableData"
        stripe
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="fullName" label="真实姓名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="用户ID" v-if="editForm.userId">
          <el-input v-model="editForm.userId" disabled />
        </el-form-item>
        <!-- 新增时可编辑用户名/密码 -->
        <el-form-item label="用户名" prop="username" v-if="isCreate">
          <el-input v-model="editForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isCreate">
          <el-input v-model="editForm.password" type="password" placeholder="请输入密码" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="isCreate">
          <el-input v-model="editForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password clearable />
        </el-form-item>
        <!-- 编辑时展示用户名（只读） -->
        <el-form-item label="用户名" v-if="!isCreate">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="fullName">
          <el-input
            v-model="editForm.fullName"
            placeholder="请输入真实姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="editForm.email"
            type="email"
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="editForm.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="管理员" :value="1" />
            <el-option label="兽医" :value="2" />
            <el-option label="宠物主人" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageQueryUsers,
  createUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
} from '@/api/user'

const loading = ref(false)
const saveLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('编辑用户')
const editFormRef = ref()

// 是否为新增
const isCreate = computed(() => !editForm.userId)

// 搜索表单
const searchForm = reactive({
  username: '',
  fullName: '',
  email: '',
  phone: '',
  role: null,
})

// 编辑/新增表单
const editForm = reactive({
  userId: null,
  username: '',
  password: '',
  confirmPassword: '',
  fullName: '',
  email: '',
  phone: '',
  role: null,
})

// 表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (isCreate.value) {
    if (!value) return callback(new Error('请再次输入密码'))
    if (value !== editForm.password) return callback(new Error('两次输入密码不一致'))
  }
  callback()
}

const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (rule, value, cb) => {
      if (isCreate.value && !value) return cb(new Error('请输入密码'))
      cb()
    } },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' },
  ],
}

// 表格数据
const tableData = ref([])

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 选中的用户
const selectedUsers = ref([])

// 获取角色名称
const getRoleName = (role) => {
  const roleMap = {
    1: '管理员',
    2: '兽医',
    3: '宠物主人',
  }
  return roleMap[role] || '未知'
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    1: 'danger',
    2: 'warning',
    3: 'success',
  }
  return typeMap[role] || 'info'
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await pageQueryUsers({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      fullName: searchForm.fullName || undefined,
      email: searchForm.email || undefined,
      phone: searchForm.phone || undefined,
      role: searchForm.role || undefined,
    })

    if (response.code === 200) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadUsers()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.fullName = ''
  searchForm.email = ''
  searchForm.phone = ''
  searchForm.role = null
  pagination.pageNum = 1
  loadUsers()
}

// 分页变化
const handlePageChange = () => {
  loadUsers()
}

// 新增用户
const handleCreate = () => {
  dialogTitle.value = '新增用户'
  // 重置表单
  editForm.userId = null
  editForm.username = ''
  editForm.password = ''
  editForm.confirmPassword = ''
  editForm.fullName = ''
  editForm.email = ''
  editForm.phone = ''
  editForm.role = null
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  editForm.userId = row.userId
  editForm.username = row.username
  editForm.password = ''
  editForm.confirmPassword = ''
  editForm.fullName = row.fullName
  editForm.email = row.email
  editForm.phone = row.phone
  editForm.role = row.role
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

// 保存用户
const handleSave = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        let response
        if (isCreate.value) {
          response = await createUser({
            username: editForm.username,
            password: editForm.password,
            fullName: editForm.fullName,
            email: editForm.email,
            phone: editForm.phone,
            role: editForm.role,
          })
        } else {
          response = await updateUser({
            userId: editForm.userId,
            fullName: editForm.fullName,
            email: editForm.email,
            phone: editForm.phone,
            role: editForm.role,
          })
        }

        if (response.code === 200) {
          ElMessage.success(isCreate.value ? '创建用户成功' : '用户信息更新成功')
          dialogVisible.value = false
          loadUsers()
        }
      } catch (error) {
        ElMessage.error(error.message || (isCreate.value ? '创建用户失败' : '更新用户失败'))
      } finally {
        saveLoading.value = false
      }
    }
  })
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const response = await deleteUser(row.userId)
        if (response.code === 200) {
          ElMessage.success('用户删除成功')
          loadUsers()
        }
      } catch (error) {
        ElMessage.error(error.message || '删除用户失败')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 批量删除用户
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请先选择要删除的用户')
    return
  }

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const userIds = selectedUsers.value.map((user) => user.userId)
        const response = await batchDeleteUsers(userIds)
        if (response.code === 200) {
          ElMessage.success('用户批量删除成功')
          selectedUsers.value = []
          loadUsers()
        }
      } catch (error) {
        ElMessage.error(error.message || '批量删除用户失败')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 对话框关闭
const handleDialogClose = () => {
  editFormRef.value?.clearValidate()
  editForm.userId = null
  editForm.username = ''
  editForm.fullName = ''
  editForm.email = ''
  editForm.phone = ''
  editForm.role = null
}

// 页面加载
onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.user-management-container {
  padding: 20px;

  .box-card {
    margin-bottom: 20px;
  }

  .search-section {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e0e0e0;

    .search-form {
      :deep(.el-form-item) {
        margin-bottom: 12px;
      }
    }
  }

  .operation-section {
    margin-bottom: 20px;
  }

  .pagination-section {
    margin-top: 20px;
    text-align: right;

    :deep(.el-pagination) {
      display: inline-flex;
    }
  }

  .ml-10 {
    margin-left: 10px;
  }
}
</style>

