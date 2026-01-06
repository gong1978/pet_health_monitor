<template>
  <div class="alert-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">异常预警管理</span>
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
          <el-form-item label="预警类型">
            <el-select v-model="searchForm.alertType" placeholder="请选择类型" clearable style="width: 150px">
              <el-option label="高温预警" value="high_temperature" />
              <el-option label="低活动预警" value="low_activity" />
              <el-option label="异常行为" value="abnormal_behavior" />
              <el-option label="心率异常" value="heart_rate_abnormal" />
              <el-option label="体温异常" value="temperature_abnormal" />
            </el-select>
          </el-form-item>
          <el-form-item label="预警等级">
            <el-select v-model="searchForm.level" placeholder="请选择等级" clearable style="width: 120px">
              <el-option label="警告" value="warning" />
              <el-option label="严重" value="critical" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="searchForm.isResolved" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="未处理" :value="false" />
              <el-option label="已处理" :value="true" />
            </el-select>
          </el-form-item>
          <el-form-item label="预警时间">
            <el-date-picker v-model="timeRange" type="datetimerange" range-separator="至" 
              start-placeholder="开始时间" end-placeholder="结束时间" format="YYYY-MM-DD HH:mm:ss" 
              value-format="YYYY-MM-DD HH:mm:ss" @change="handleTimeRangeChange" style="width: 350px" />
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
            <el-icon><Plus /></el-icon>新增预警
          </el-button>
          <el-button type="success" :disabled="multipleSelection.length === 0" @click="handleBatchResolve">
            <el-icon><Check /></el-icon>批量处理
          </el-button>
          <el-button type="danger" :disabled="multipleSelection.length === 0" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="alertId" label="预警ID" width="80" />
        <el-table-column prop="petName" label="宠物名字" min-width="120" />
        <el-table-column prop="alertType" label="预警类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeTagType(row.alertType)">{{ getAlertTypeName(row.alertType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="预警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === 'critical' ? 'danger' : 'warning'">
              {{ row.level === 'critical' ? '严重' : '警告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertMessage" label="预警内容" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.alertMessage" placement="top" v-if="row.alertMessage">
              <span class="text-ellipsis">{{ row.alertMessage }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="isResolved" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isResolved ? 'success' : 'info'">
              {{ row.isResolved ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="预警时间" width="160" />
        <el-table-column prop="resolverName" label="处理人员" width="120">
          <template #default="{ row }">{{ row.resolverName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.isResolved" size="small" type="success" @click="handleResolve(row)">处理</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-popconfirm title="确定要删除这条预警吗？" @confirm="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="alertFormRef" :model="alertForm" :rules="rules" label-width="100px">
        <el-form-item label="宠物" prop="petId">
          <el-select v-model="alertForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.petId" :label="pet.name" :value="pet.petId" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警类型" prop="alertType">
          <el-select v-model="alertForm.alertType" placeholder="请选择预警类型" style="width: 100%">
            <el-option label="高温预警" value="high_temperature" />
            <el-option label="低活动预警" value="low_activity" />
            <el-option label="异常行为" value="abnormal_behavior" />
            <el-option label="心率异常" value="heart_rate_abnormal" />
            <el-option label="体温异常" value="temperature_abnormal" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警等级" prop="level">
          <el-radio-group v-model="alertForm.level">
            <el-radio label="warning">警告</el-radio>
            <el-radio label="critical">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预警内容" prop="alertMessage">
          <el-input v-model="alertForm.alertMessage" type="textarea" :rows="4" 
            placeholder="请输入预警内容描述" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="预警时间">
          <el-date-picker v-model="alertForm.createdAt" type="datetime" placeholder="选择预警时间" 
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
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
    <el-dialog v-model="detailDialogVisible" title="预警详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预警ID">{{ viewAlert.alertId }}</el-descriptions-item>
        <el-descriptions-item label="宠物名字">{{ viewAlert.petName }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">
          <el-tag :type="getAlertTypeTagType(viewAlert.alertType)">{{ getAlertTypeName(viewAlert.alertType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警等级">
          <el-tag :type="viewAlert.level === 'critical' ? 'danger' : 'warning'">
            {{ viewAlert.level === 'critical' ? '严重' : '警告' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态" :span="2">
          <el-tag :type="viewAlert.isResolved ? 'success' : 'info'">
            {{ viewAlert.isResolved ? '已处理' : '未处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警时间" :span="2">{{ viewAlert.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="处理人员" :span="2">{{ viewAlert.resolverName || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="预警内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
            {{ viewAlert.alertMessage || '-' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Check } from '@element-plus/icons-vue'
import { getAlerts, createAlert, updateAlert, deleteAlert, batchDeleteAlerts, resolveAlert, batchResolveAlerts } from '@/api/alert'
import { getPets } from '@/api/pet'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const multipleSelection = ref([])
const alertFormRef = ref()
const petList = ref([])
const timeRange = ref([])

// 搜索表单
const searchForm = reactive({
  petId: null,       // 改为 null
  alertType: null,   // 改为 null
  level: null,
  isResolved: null,  // 改为 null
  startTime: null,
  endTime: null
})

// 分页信息
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 预警表单
const alertForm = reactive({
  alertId: null,
  petId: null,
  alertType: '',
  level: 'warning',
  alertMessage: '',
  createdAt: ''
})

// 查看预警详情
const viewAlert = ref({})

// 表单验证规则
const rules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  alertType: [{ required: true, message: '请选择预警类型', trigger: 'change' }],
  alertMessage: [{ required: true, message: '请输入预警内容', trigger: 'blur' }]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑预警' : '新增预警')

// 获取预警类型名称
const getAlertTypeName = (type) => {
  const typeMap = {
    'high_temperature': '高温预警',
    'low_activity': '低活动预警',
    'abnormal_behavior': '异常行为',
    'heart_rate_abnormal': '心率异常',
    'temperature_abnormal': '体温异常'
  }
  return typeMap[type] || type
}

// 获取预警类型标签样式
const getAlertTypeTagType = (type) => {
  const typeMap = {
    'high_temperature': 'danger',
    'low_activity': 'warning',
    'abnormal_behavior': 'primary',
    'heart_rate_abnormal': 'danger',
    'temperature_abnormal': 'danger'
  }
  return typeMap[type] || ''
}

// 获取宠物列表
const fetchPetList = async () => {
  try {
    const response = await getPets({ page: 1, size: 1000 })
    if (response.code === 200) {
      petList.value = response.data.records
    }
  } catch (error) {
    console.error('获取宠物列表失败:', error)
  }
}

// 获取预警列表（修复版）
const fetchAlerts = async () => {
  loading.value = true
  try {
    // 1. 准备原始参数
    const rawParams = {
      page: pageInfo.page,
      size: pageInfo.size,
      petId: searchForm.petId,
      alertType: searchForm.alertType,
      level: searchForm.level,
      isResolved: searchForm.isResolved,
      startTime: searchForm.startTime,
      endTime: searchForm.endTime
    }

    // 2. [核心修复] 清洗参数：把空字符串 "" 剔除掉
    const params = {}
    for (const key in rawParams) {
      // 只有当值不是空字符串、不是null、不是undefined时，才发送
      if (rawParams[key] !== '' && rawParams[key] !== null && rawParams[key] !== undefined) {
        params[key] = rawParams[key]
      }
    }

    // 3. 发送干净的请求
    const response = await getAlerts(params)

    if (response.code === 200) {
      tableData.value = response.data.records
      pageInfo.total = response.data.total
    } else {
      ElMessage.error(response.message || '获取预警列表失败')
    }
  } catch (error) {
    console.error('Fetch alerts error:', error)
    if (error.response && error.response.status === 400) {
      // 如果还报400，说明代码没保存成功或者没生效
      console.error("400错误详情:", error.response)
    }
    tableData.value = []
    pageInfo.total = 0
  } finally {
    loading.value = false
  }
}

// 时间范围变化
const handleTimeRangeChange = (value) => {
  if (value && value.length === 2) {
    searchForm.startTime = value[0]
    searchForm.endTime = value[1]
  } else {
    searchForm.startTime = ''
    searchForm.endTime = ''
  }
}

// 搜索
const handleSearch = () => {
  pageInfo.page = 1
  fetchAlerts()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    petId: null, alertType: '', level: '', isResolved: null, startTime: '', endTime: ''
  })
  timeRange.value = []
  pageInfo.page = 1
  fetchAlerts()
}

// 分页变化
const handleSizeChange = (size) => {
  pageInfo.size = size
  pageInfo.page = 1
  fetchAlerts()
}

const handleCurrentChange = (page) => {
  pageInfo.page = page
  fetchAlerts()
}

// 选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(alertForm, {
    alertId: row.alertId,
    petId: row.petId,
    alertType: row.alertType,
    level: row.level,
    alertMessage: row.alertMessage,
    createdAt: row.createdAt
  })
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  viewAlert.value = { ...row }
  detailDialogVisible.value = true
}

// 处理预警
const handleResolve = async (row) => {
  try {
    await resolveAlert(row.alertId)
    ElMessage.success('预警处理成功')
    fetchAlerts()
  } catch (error) {
    ElMessage.error(error.message || '预警处理失败')
  }
}

// 批量处理
const handleBatchResolve = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要处理的预警')
    return
  }

  try {
    await ElMessageBox.confirm('确定要处理选中的预警吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })

    const alertIds = multipleSelection.value.map(item => item.alertId)
    await batchResolveAlerts(alertIds)
    ElMessage.success('批量处理预警成功')
    fetchAlerts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量处理预警失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await deleteAlert(row.alertId)
    ElMessage.success('删除预警成功')
    fetchAlerts()
  } catch (error) {
    ElMessage.error(error.message || '删除预警失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的预警')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除选中的预警吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })

    const alertIds = multipleSelection.value.map(item => item.alertId)
    await batchDeleteAlerts(alertIds)
    ElMessage.success('批量删除预警成功')
    fetchAlerts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除预警失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!alertFormRef.value) return

  await alertFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...alertForm }
        delete data.alertId

        if (isEdit.value) {
          await updateAlert(alertForm.alertId, data)
          ElMessage.success('更新预警成功')
        } else {
          await createAlert(data)
          ElMessage.success('创建预警成功')
        }

        dialogVisible.value = false
        fetchAlerts()
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
  Object.assign(alertForm, {
    alertId: null, petId: null, alertType: '', level: 'warning', alertMessage: '', createdAt: ''
  })
  if (alertFormRef.value) {
    alertFormRef.value.clearValidate()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPetList()
  fetchAlerts()
})
</script>

<style scoped lang="scss">
.alert-management {
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

@media (max-width: 768px) {
  .alert-management {
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
