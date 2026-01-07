<template>
  <div class="health-report-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">健康报告管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="宠物">
            <el-select
              v-model="searchForm.petId"
              placeholder="请选择宠物"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="pet in petList"
                :key="pet.petId"
                :label="pet.name"
                :value="pet.petId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="健康评分">
            <el-input
              v-model.number="searchForm.minHealthScore"
              placeholder="最小值"
              style="width: 80px"
              type="number"
              :min="0"
              :max="100"
            />
            <span style="margin: 0 5px;">-</span>
            <el-input
              v-model.number="searchForm.maxHealthScore"
              placeholder="最大值"
              style="width: 80px"
              type="number"
              :min="0"
              :max="100"
            />
            <span style="margin-left: 5px; color: #999;">分</span>
          </el-form-item>
          <el-form-item label="审核兽医">
            <el-select
              v-model="searchForm.reviewedBy"
              placeholder="请选择兽医"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="vet in vetList"
                :key="vet.userId"
                :label="vet.fullName || vet.username"
                :value="vet.userId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报告日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateRangeChange"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作按钮 - 宠物主人只能查看，不能进行操作 -->
      <div class="table-header" v-if="isAdmin || isVet">
        <div class="left-panel">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增报告
          </el-button>
          <el-button 
            type="danger" 
            :disabled="multipleSelection.length === 0" 
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column v-if="isAdmin || isVet" type="selection" width="55" />
        <el-table-column prop="reportId" label="报告ID" width="80" />
        <el-table-column prop="petName" label="宠物名字" min-width="120" />
        <el-table-column prop="reportDate" label="报告日期" width="120" />
        <el-table-column prop="healthScore" label="健康评分" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getScoreTagType(row.healthScore)" 
              v-if="row.healthScore !== null && row.healthScore !== undefined"
            >
              {{ row.healthScore }}分
            </el-tag>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="健康总结" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.summary" placement="top" v-if="row.summary">
              <span class="text-ellipsis">{{ row.summary }}</span>
            </el-tooltip>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewerName" label="审核兽医" width="120">
          <template #default="{ row }">
            {{ row.reviewerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isPetOwner ? '80' : '220'" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isAdmin || isVet" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-popconfirm
              v-if="isAdmin || isVet"
              title="确定要删除这份报告吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageInfo.page"
          v-model:page-size="pageInfo.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pageInfo.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="healthReportFormRef"
        :model="healthReportForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="宠物" prop="petId">
          <el-select v-model="healthReportForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option
              v-for="pet in petList"
              :key="pet.petId"
              :label="pet.name"
              :value="pet.petId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报告日期" prop="reportDate">
          <el-date-picker
            v-model="healthReportForm.reportDate"
            type="date"
            placeholder="选择报告日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="健康评分" prop="healthScore">
          <el-input-number 
            v-model="healthReportForm.healthScore" 
            :min="0" 
            :max="100" 
            controls-position="right"
            placeholder="请输入健康评分"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999;">分 (0-100)</span>
        </el-form-item>
        <el-form-item label="健康总结" prop="summary">
          <el-input
            v-model="healthReportForm.summary"
            type="textarea"
            :rows="4"
            placeholder="请输入健康总结"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="健康建议" prop="suggestions">
          <el-input
            v-model="healthReportForm.suggestions"
            type="textarea"
            :rows="4"
            placeholder="请输入健康/饲养建议"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="审核兽医" prop="reviewedBy">
          <el-select v-model="healthReportForm.reviewedBy" placeholder="请选择审核兽医" style="width: 100%">
            <el-option label="无审核" :value="null" />
            <el-option
              v-for="vet in vetList"
              :key="vet.userId"
              :label="vet.fullName || vet.username"
              :value="vet.userId"
            />
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="报告详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报告ID">{{ viewReport.reportId }}</el-descriptions-item>
        <el-descriptions-item label="宠物名字">{{ viewReport.petName }}</el-descriptions-item>
        <el-descriptions-item label="报告日期">{{ viewReport.reportDate }}</el-descriptions-item>
        <el-descriptions-item label="健康评分">
          <el-tag :type="getScoreTagType(viewReport.healthScore)" v-if="viewReport.healthScore !== null && viewReport.healthScore !== undefined">
            {{ viewReport.healthScore }}分
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核兽医" :span="2">{{ viewReport.reviewerName || '无审核' }}</el-descriptions-item>
        <el-descriptions-item label="健康总结" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
            {{ viewReport.summary || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="健康建议" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
            {{ viewReport.suggestions || '-' }}
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
import { getHealthReports, getHealthReportById, createHealthReport, updateHealthReport, deleteHealthReport, batchDeleteHealthReports } from '@/api/healthReport'
import { getPets } from '@/api/pet'
import { getUsers } from '@/api/user'
import { getMyPets } from '@/api/userPet'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const multipleSelection = ref([])
const healthReportFormRef = ref()
const petList = ref([])
const vetList = ref([])
const dateRange = ref([])

// 搜索表单
const searchForm = reactive({
  petId: null,
  minHealthScore: null,
  maxHealthScore: null,
  reviewedBy: null,
  startDate: '',
  endDate: ''
})

// 分页信息
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 健康报告表单
const healthReportForm = reactive({
  reportId: null,
  petId: null,
  reportDate: '',
  healthScore: null,
  summary: '',
  suggestions: '',
  reviewedBy: null
})

// 查看报告详情
const viewReport = ref({})

// 表单验证规则
const rules = {
  petId: [
    { required: true, message: '请选择宠物', trigger: 'change' }
  ],
  reportDate: [
    { required: true, message: '请选择报告日期', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑报告' : '新增报告')

// 用户角色判断
const isAdmin = computed(() => authStore.userInfo?.role === 1)
const isVet = computed(() => authStore.userInfo?.role === 2)
const isPetOwner = computed(() => {
  const role = authStore.userInfo?.role
  return role === 3 || role === 0 || (!isAdmin.value && !isVet.value)
})

// 根据评分获取标签类型
const getScoreTagType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 70) return 'primary'
  if (score >= 50) return 'warning'
  return 'danger'
}

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

// 获取健康报告列表
const fetchHealthReports = async () => {
  loading.value = true
  try {
    console.log('当前用户角色判断:', { isAdmin: isAdmin.value, isVet: isVet.value, isPetOwner: isPetOwner.value })
    console.log('宠物列表:', petList.value)
    
    if (isPetOwner.value) {
      // 宠物主人：只获取自己宠物的健康报告
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
      
      const response = await getHealthReports(params)
      if (response.code === 200) {
        let allRecords = response.data.records || []
        console.log('获取到的所有健康报告数据:', allRecords)
        
        // 前端过滤：只保留用户宠物的数据
        const filteredRecords = allRecords.filter(record => {
          const recordPetId = record.petId || record.pet_id
          const isMatch = userPetIds.includes(recordPetId)
          console.log(`记录 ${record.reportId}: petId=${recordPetId}, 是否匹配=${isMatch}`)
          return isMatch
        })
        
        console.log('过滤后的健康报告数据:', filteredRecords)
        
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
      
      const response = await getHealthReports(params)
      if (response.code === 200) {
        console.log('管理员/兽医获取到的健康报告数据:', response.data)
        tableData.value = response.data.records || []
        pageInfo.total = response.data.total || 0
      }
    }
  } catch (error) {
    console.error('获取健康报告数据失败:', error)
    ElMessage.error(error.message || '获取报告列表失败')
    tableData.value = []
    pageInfo.total = 0
  } finally {
    loading.value = false
  }
}

// 日期范围变化
const handleDateRangeChange = (value) => {
  if (value && value.length === 2) {
    searchForm.startDate = value[0]
    searchForm.endDate = value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
}

// 搜索
const handleSearch = () => {
  pageInfo.page = 1
  fetchHealthReports()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    petId: null,
    minHealthScore: null,
    maxHealthScore: null,
    reviewedBy: null,
    startDate: '',
    endDate: ''
  })
  dateRange.value = []
  pageInfo.page = 1
  fetchHealthReports()
}

// 分页变化
const handleSizeChange = (size) => {
  pageInfo.size = size
  pageInfo.page = 1
  fetchHealthReports()
}

const handleCurrentChange = (page) => {
  pageInfo.page = page
  fetchHealthReports()
}

// 选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 新增
const handleAdd = () => {
  // 权限检查：宠物主人不能新增
  if (isPetOwner.value) {
    ElMessage.error('您没有权限新增健康报告')
    return
  }
  
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  // 权限检查：宠物主人不能编辑
  if (isPetOwner.value) {
    ElMessage.error('您没有权限编辑健康报告')
    return
  }
  
  isEdit.value = true
  Object.assign(healthReportForm, {
    reportId: row.reportId,
    petId: row.petId,
    reportDate: row.reportDate,
    healthScore: row.healthScore,
    summary: row.summary || '',
    suggestions: row.suggestions || '',
    reviewedBy: row.reviewedBy
  })
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  viewReport.value = { ...row }
  detailDialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  // 权限检查：宠物主人不能删除
  if (isPetOwner.value) {
    ElMessage.error('您没有权限删除健康报告')
    return
  }
  
  try {
    await deleteHealthReport(row.reportId)
    ElMessage.success('删除报告成功')
    fetchHealthReports()
  } catch (error) {
    ElMessage.error(error.message || '删除报告失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  // 权限检查：宠物主人不能批量删除
  if (isPetOwner.value) {
    ElMessage.error('您没有权限批量删除健康报告')
    return
  }
  
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的报告')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除选中的报告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const reportIds = multipleSelection.value.map(item => item.reportId)
    await batchDeleteHealthReports(reportIds)
    ElMessage.success('批量删除报告成功')
    fetchHealthReports()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除报告失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!healthReportFormRef.value) return

  await healthReportFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...healthReportForm }
        delete data.reportId

        if (isEdit.value) {
          await updateHealthReport(healthReportForm.reportId, data)
          ElMessage.success('更新报告成功')
        } else {
          await createHealthReport(data)
          ElMessage.success('创建报告成功')
        }

        dialogVisible.value = false
        fetchHealthReports()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(healthReportForm, {
    reportId: null,
    petId: null,
    reportDate: '',
    healthScore: null,
    summary: '',
    suggestions: '',
    reviewedBy: null
  })
  if (healthReportFormRef.value) {
    healthReportFormRef.value.clearValidate()
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  // 先获取宠物列表，再获取健康报告数据
  await fetchPetList()
  fetchVetList()
  fetchHealthReports()
})
</script>

<style scoped lang="scss">
.health-report-management {
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

// 响应式设计
@media (max-width: 768px) {
  .health-report-management {
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
