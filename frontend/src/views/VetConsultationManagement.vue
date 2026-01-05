<template>
  <div class="vet-consultation-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">兽医咨询管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="宠物">
            <el-select v-model="searchForm.petId" placeholder="请选择宠物" clearable style="width: 150px">
              <el-option v-for="pet in petList" :key="pet.petId" :label="pet.name" :value="pet.petId" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="isAdmin || isVet" label="提问用户">
            <el-select v-model="searchForm.userId" placeholder="请选择用户" clearable style="width: 150px">
              <el-option v-for="user in userList" :key="user.userId" :label="user.fullName || user.username" :value="user.userId" />
            </el-select>
          </el-form-item>
          <el-form-item label="回答兽医">
            <el-select v-model="searchForm.answeredBy" placeholder="请选择兽医" clearable style="width: 150px">
              <el-option v-for="vet in vetList" :key="vet.userId" :label="vet.fullName || vet.username" :value="vet.userId" />
            </el-select>
          </el-form-item>
          <el-form-item label="回答状态">
            <el-select v-model="searchForm.answered" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="未回答" :value="false" />
              <el-option label="已回答" :value="true" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="搜索问题内容" style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作按钮 -->
      <div class="table-header">
        <div class="left-panel">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增咨询
          </el-button>
          <el-button 
            v-if="isAdmin || isVet"
            type="danger" 
            :disabled="multipleSelection.length === 0" 
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column v-if="isAdmin || isVet" type="selection" width="55" />
        <el-table-column prop="consultId" label="咨询ID" width="80" />
        <el-table-column prop="petName" label="宠物名字" min-width="120">
          <template #default="{ row }">{{ row.petName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="提问用户" min-width="120">
          <template #default="{ row }">{{ row.userName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="question" label="咨询问题" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.question" placement="top" v-if="row.question">
              <span class="text-ellipsis">{{ row.question }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="answered" label="回答状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.answered ? 'success' : 'warning'">
              {{ row.answered ? '已回答' : '未回答' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="askedAt" label="提问时间" width="160" />
        <el-table-column prop="answeredByName" label="回答兽医" width="120">
          <template #default="{ row }">{{ row.answeredByName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="isPetOwner ? '120' : '220'" fixed="right">
          <template #default="{ row }">
            <el-button v-if="(isAdmin || isVet) && !row.answered" size="small" type="success" @click="handleAnswer(row)">回答</el-button>
            <el-button v-if="isAdmin || isVet" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-popconfirm title="确定要删除这条咨询吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination">
        <el-pagination v-model:current-page="pageInfo.page" v-model:page-size="pageInfo.size" 
          :page-sizes="[10, 20, 50, 100]" :total="pageInfo.total" 
          layout="total, sizes, prev, pager, next, jumper" 
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="consultFormRef" :model="consultForm" :rules="rules" label-width="100px">
        <el-form-item label="宠物">
          <el-select v-model="consultForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.petId" :label="pet.name" :value="pet.petId" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdmin || isVet" label="提问用户">
          <el-select v-model="consultForm.userId" placeholder="请选择用户" style="width: 100%">
            <el-option v-for="user in userList" :key="user.userId" :label="user.fullName || user.username" :value="user.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询问题" prop="question">
          <el-input v-model="consultForm.question" type="textarea" :rows="4" 
            placeholder="请输入咨询问题" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="兽医回复" v-if="isEdit">
          <el-input v-model="consultForm.answer" type="textarea" :rows="4" 
            placeholder="请输入兽医回复" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="回答兽医" v-if="isEdit">
          <el-select v-model="consultForm.answeredBy" placeholder="请选择回答兽医" style="width: 100%">
            <el-option label="无回答" :value="null" />
            <el-option v-for="vet in vetList" :key="vet.userId" :label="vet.fullName || vet.username" :value="vet.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 回答对话框 -->
    <el-dialog v-model="answerDialogVisible" title="兽医回答" width="600px" :close-on-click-modal="false">
      <div class="answer-content">
        <el-descriptions title="咨询信息" :column="1" border>
          <el-descriptions-item label="宠物名字">{{ currentConsult.petName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提问用户">{{ currentConsult.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提问时间">{{ currentConsult.askedAt }}</el-descriptions-item>
          <el-descriptions-item label="咨询问题">
            <div style="white-space: pre-wrap; max-height: 150px; overflow-y: auto;">
              {{ currentConsult.question }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <label style="font-weight: bold; margin-bottom: 10px; display: block;">兽医回答：</label>
          <el-input v-model="answerContent" type="textarea" :rows="5" 
            placeholder="请输入您的专业回答" maxlength="1000" show-word-limit />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="answerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitAnswer" :loading="submitting">提交回答</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="咨询详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="咨询ID">{{ viewConsult.consultId }}</el-descriptions-item>
        <el-descriptions-item label="宠物名字">{{ viewConsult.petName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提问用户">{{ viewConsult.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="回答状态">
          <el-tag :type="viewConsult.answered ? 'success' : 'warning'">
            {{ viewConsult.answered ? '已回答' : '未回答' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提问时间" :span="2">{{ viewConsult.askedAt }}</el-descriptions-item>
        <el-descriptions-item label="回答兽医" :span="2">{{ viewConsult.answeredByName || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="咨询问题" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
            {{ viewConsult.question || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="兽医回复" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
            {{ viewConsult.answer || '暂无回复' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getVetConsultations, createVetConsultation, updateVetConsultation, deleteVetConsultation, batchDeleteVetConsultations, answerConsultation } from '@/api/vetConsultation'
import { getPets } from '@/api/pet'
import { getUsers } from '@/api/user'
import { getMyPets } from '@/api/userPet'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const answerDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const multipleSelection = ref([])
const consultFormRef = ref()
const petList = ref([])
const userList = ref([])
const vetList = ref([])
const answerContent = ref('')
const currentConsult = ref({})

// 搜索表单
const searchForm = reactive({
  petId: null, userId: null, answeredBy: null, answered: null, keyword: ''
})

// 分页信息
const pageInfo = reactive({ page: 1, size: 10, total: 0 })

// 咨询表单
const consultForm = reactive({
  consultId: null, petId: null, userId: null, question: '', answer: '', answeredBy: null
})

// 查看咨询详情
const viewConsult = ref({})

// 表单验证规则
const rules = {
  question: [{ required: true, message: '请输入咨询问题', trigger: 'blur' }]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑咨询' : '新增咨询')

// 用户角色判断
const isAdmin = computed(() => authStore.userInfo?.role === 1)
const isVet = computed(() => authStore.userInfo?.role === 2)
const isPetOwner = computed(() => {
  const role = authStore.userInfo?.role
  return role === 3 || role === 0 || (!isAdmin.value && !isVet.value)
})

// 获取宠物列表
const fetchPetList = async () => {
  try {
    console.log('获取宠物列表，当前用户角色:', { isAdmin: isAdmin.value, isVet: isVet.value, isPetOwner: isPetOwner.value })
    
    let response
    if (isPetOwner.value) {
      // 宠物主人：只获取自己关联的宠物
      console.log('宠物主人调用 getMyPets()')
      response = await getMyPets()
      if (response.code === 200) {
        petList.value = response.data || []
        console.log('宠物主人获取到宠物列表:', petList.value)
      }
    } else {
      // 管理员和兽医：获取所有宠物
      console.log('管理员/兽医调用 getPets()')
      response = await getPets({ page: 1, size: 1000 })
      if (response.code === 200) {
        petList.value = response.data.records || []
        console.log('管理员/兽医获取到宠物列表:', petList.value)
      }
    }
  } catch (error) {
    console.error('获取宠物列表失败:', error)
    petList.value = []
  }
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    const response = await getUsers({ page: 1, size: 1000 })
    if (response.code === 200) {
      userList.value = response.data.records
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 获取兽医列表
const fetchVetList = async () => {
  try {
    const response = await getUsers({ page: 1, size: 1000, role: 2 })
    if (response.code === 200) {
      vetList.value = response.data.records
    }
  } catch (error) {
    console.error('获取兽医列表失败:', error)
  }
}

// 获取咨询列表
const fetchConsultations = async () => {
  loading.value = true
  try {
    console.log('当前用户角色判断:', { isAdmin: isAdmin.value, isVet: isVet.value, isPetOwner: isPetOwner.value })
    console.log('宠物列表:', petList.value)
    
    if (isPetOwner.value) {
      // 宠物主人：只获取自己宠物的咨询记录
      const userPetIds = petList.value.map(pet => pet.petId || pet.id)
      console.log('宠物主人的宠物ID列表:', userPetIds)
      
      if (userPetIds.length === 0) {
        // 如果没有宠物，显示空数据
        console.log('宠物主人没有关联宠物，显示空数据')
        tableData.value = []
        pageInfo.total = 0
        return
      }
      
      // 获取所有数据然后前端过滤
      let params = {
        ...searchForm,
        page: 1,
        size: 1000 // 获取更多数据用于过滤
      }
      console.log('宠物主人查询参数:', params)
      
      const response = await getVetConsultations(params)
      if (response.code === 200) {
        let allRecords = response.data.records || []
        console.log('获取到的所有咨询数据:', allRecords)
        
        // 前端过滤：只保留用户宠物的数据
        const filteredRecords = allRecords.filter(record => {
          const recordPetId = record.petId || record.pet_id
          const isMatch = userPetIds.includes(recordPetId)
          console.log(`记录 ${record.consultId}: petId=${recordPetId}, 是否匹配=${isMatch}`)
          return isMatch
        })
        
        console.log('过滤后的咨询数据:', filteredRecords)
        
        // 手动分页
        const total = filteredRecords.length
        const start = (pageInfo.page - 1) * pageInfo.size
        const end = start + pageInfo.size
        const paginatedRecords = filteredRecords.slice(start, end)
        
        tableData.value = paginatedRecords
        pageInfo.total = total
      }
    } else {
      // 管理员和兽医：获取所有数据
      const params = {
        ...searchForm,
        page: pageInfo.page,
        size: pageInfo.size
      }
      console.log('管理员/兽医查询参数:', params)
      
      const response = await getVetConsultations(params)
      if (response.code === 200) {
        console.log('管理员/兽医获取到的咨询数据:', response.data)
        tableData.value = response.data.records || []
        pageInfo.total = response.data.total || 0
      }
    }
  } catch (error) {
    console.error('获取咨询数据失败:', error)
    ElMessage.error(error.message || '获取咨询列表失败')
    tableData.value = []
    pageInfo.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageInfo.page = 1
  fetchConsultations()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, { petId: null, userId: null, answeredBy: null, answered: null, keyword: '' })
  pageInfo.page = 1
  fetchConsultations()
}

// 分页变化
const handleSizeChange = (size) => {
  pageInfo.size = size
  pageInfo.page = 1
  fetchConsultations()
}

const handleCurrentChange = (page) => {
  pageInfo.page = page
  fetchConsultations()
}

// 选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  
  // 宠物主人自动设置为当前登录用户
  if (isPetOwner.value) {
    consultForm.userId = authStore.userInfo?.userId || authStore.userInfo?.id
  }
  
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  // 权限检查：宠物主人不能编辑
  if (isPetOwner.value) {
    ElMessage.error('您没有权限编辑咨询记录')
    return
  }
  
  isEdit.value = true
  Object.assign(consultForm, {
    consultId: row.consultId, petId: row.petId, userId: row.userId,
    question: row.question, answer: row.answer || '', answeredBy: row.answeredBy
  })
  dialogVisible.value = true
}

// 回答
const handleAnswer = (row) => {
  currentConsult.value = { ...row }
  answerContent.value = ''
  answerDialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  viewConsult.value = { ...row }
  detailDialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await deleteVetConsultation(row.consultId)
    ElMessage.success('删除咨询成功')
    fetchConsultations()
  } catch (error) {
    ElMessage.error(error.message || '删除咨询失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  // 权限检查：宠物主人不能批量删除
  if (isPetOwner.value) {
    ElMessage.error('您没有权限批量删除咨询')
    return
  }
  
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的咨询')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除选中的咨询吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })

    const consultIds = multipleSelection.value.map(item => item.consultId)
    await batchDeleteVetConsultations(consultIds)
    ElMessage.success('批量删除咨询成功')
    fetchConsultations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除咨询失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!consultFormRef.value) return

  await consultFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...consultForm }
        delete data.consultId

        if (isEdit.value) {
          await updateVetConsultation(consultForm.consultId, data)
          ElMessage.success('更新咨询成功')
        } else {
          await createVetConsultation(data)
          ElMessage.success('创建咨询成功')
        }

        dialogVisible.value = false
        fetchConsultations()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 提交回答
const handleSubmitAnswer = async () => {
  if (!answerContent.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }

  submitting.value = true
  try {
    await answerConsultation(currentConsult.value.consultId, answerContent.value)
    ElMessage.success('回答提交成功')
    answerDialogVisible.value = false
    fetchConsultations()
  } catch (error) {
    ElMessage.error(error.message || '回答提交失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(consultForm, {
    consultId: null, petId: null, userId: null, question: '', answer: '', answeredBy: null
  })
  if (consultFormRef.value) {
    consultFormRef.value.clearValidate()
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  // 先获取宠物列表，再获取咨询数据
  await fetchPetList()
  fetchUserList()
  fetchVetList()
  fetchConsultations()
})
</script>

<style scoped lang="scss">
.vet-consultation-management {
  padding: 20px;
}

.page-container {
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

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .left-panel {
    .el-button {
      margin-right: 10px;
    }
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

.text-ellipsis {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.answer-content {
  .el-descriptions {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .vet-consultation-management {
    padding: 10px;
  }

  .search-form {
    .el-form-item {
      display: block;
      margin-right: 0;
    }
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
