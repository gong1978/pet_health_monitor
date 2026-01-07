<template>
  <div class="sensor-data-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">数据采集管理</span>
        </div>
      </template>

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
          <el-form-item label="心率范围">
            <el-input
                v-model.number="searchForm.minHeartRate"
                placeholder="最小值"
                style="width: 80px"
                type="number"
            />
            <span style="margin: 0 5px;">-</span>
            <el-input
                v-model.number="searchForm.maxHeartRate"
                placeholder="最大值"
                style="width: 80px"
                type="number"
            />
            <span style="margin-left: 5px; color: #999;">bpm</span>
          </el-form-item>
          <el-form-item label="体温范围">
            <el-input
                v-model.number="searchForm.minTemperature"
                placeholder="最小值"
                style="width: 80px"
                type="number"
                :step="0.1"
            />
            <span style="margin: 0 5px;">-</span>
            <el-input
                v-model.number="searchForm.maxTemperature"
                placeholder="最大值"
                style="width: 80px"
                type="number"
                :step="0.1"
            />
            <span style="margin-left: 5px; color: #999;">℃</span>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
                v-model="timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                @change="handleTimeRangeChange"
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

      <div class="table-header">
        <div class="left-panel">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增数据
          </el-button>
          <el-button
              v-if="isAdmin || isVet"
              type="danger"
              :disabled="multipleSelection.length === 0"
              @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>

      <el-table
          :data="tableData"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          style="width: 100%"
      >
        <el-table-column v-if="isAdmin || isVet" type="selection" width="55" />
        <el-table-column prop="dataId" label="数据ID" width="80" />
        <el-table-column prop="petName" label="宠物名字" min-width="120" />
        <el-table-column prop="heartRate" label="心率" width="100">
          <template #default="{ row }">
            <span v-if="row.heartRate">{{ row.heartRate }} bpm</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="体温" width="100">
          <template #default="{ row }">
            <span v-if="row.temperature">{{ row.temperature }}℃</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="activity" label="活动量" width="100">
          <template #default="{ row }">
            <span v-if="row.activity !== null && row.activity !== undefined">{{ row.activity }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="collectedAt" label="采集时间" width="160" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-popconfirm
                title="确定要删除这条数据吗？"
                @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="sensorDataFormRef"
          :model="sensorDataForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="宠物" prop="petId">
          <el-select v-model="sensorDataForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option
                v-for="pet in petList"
                :key="pet.petId"
                :label="pet.name"
                :value="pet.petId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="心率" prop="heartRate">
          <el-input-number
              v-model="sensorDataForm.heartRate"
              :min="0"
              :max="300"
              controls-position="right"
              placeholder="请输入心率"
              style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999;">bpm (0-300)</span>
        </el-form-item>
        <el-form-item label="体温" prop="temperature">
          <el-input-number
              v-model="sensorDataForm.temperature"
              :min="30"
              :max="45"
              :precision="1"
              :step="0.1"
              controls-position="right"
              placeholder="请输入体温"
              style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999;">℃ (30-45)</span>
        </el-form-item>

        <el-form-item label="活动量">
          <div v-if="!isEdit" style="color: #E6A23C; display: flex; align-items: center; padding-top: 5px;">
            <el-icon style="margin-right: 5px;"><InfoFilled /></el-icon>
            <span>将由系统自动模拟生成 (0-2000)</span>
          </div>
          <div v-else>
            <el-input-number
                v-model="sensorDataForm.activity"
                disabled
                controls-position="right"
                style="width: 100%"
            />
            <span style="margin-left: 10px; color: #999;">(历史数据不可修改)</span>
          </div>
        </el-form-item>

        <el-form-item label="采集时间" prop="collectedAt">
          <el-date-picker
              v-model="sensorDataForm.collectedAt"
              type="datetime"
              placeholder="选择采集时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            留空则使用当前时间
          </div>
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

    <el-dialog v-model="detailDialogVisible" title="数据详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="数据ID">{{ viewData.dataId }}</el-descriptions-item>
        <el-descriptions-item label="宠物名字">{{ viewData.petName }}</el-descriptions-item>
        <el-descriptions-item label="心率">{{ viewData.heartRate ? `${viewData.heartRate} bpm` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="体温">{{ viewData.temperature ? `${viewData.temperature}℃` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="活动量" :span="2">{{ viewData.activity !== null && viewData.activity !== undefined ? viewData.activity : '-' }}</el-descriptions-item>
        <el-descriptions-item label="采集时间" :span="2">{{ viewData.collectedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, InfoFilled } from '@element-plus/icons-vue' // [新增] 引入 InfoFilled 图标
import { getSensorData, createSensorData, updateSensorData, deleteSensorData, batchDeleteSensorData } from '@/api/sensorData'
import { getPets } from '@/api/pet'
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
const sensorDataFormRef = ref()
const petList = ref([])
const timeRange = ref([])

// 搜索表单
const searchForm = reactive({
  petId: null,
  minHeartRate: null,
  maxHeartRate: null,
  minTemperature: null,
  maxTemperature: null,
  minActivity: null,
  maxActivity: null,
  startTime: '',
  endTime: ''
})

// 分页信息
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 传感器数据表单
const sensorDataForm = reactive({
  dataId: null,
  petId: null,
  heartRate: null,
  temperature: null,
  activity: null,
  collectedAt: ''
})

// 查看数据详情
const viewData = ref({})

// 表单验证规则
const rules = {
  petId: [
    { required: true, message: '请选择宠物', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑数据' : '新增数据')

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
    let response
    if (isPetOwner.value) {
      // 宠物主人：只获取自己关联的宠物
      response = await getMyPets()
      if (response.code === 200) {
        petList.value = response.data || []
      }
    } else {
      // 管理员和兽医：获取所有宠物
      response = await getPets({ page: 1, size: 1000 })
      if (response.code === 200) {
        petList.value = response.data.records || []
      }
    }
  } catch (error) {
    console.error('获取宠物列表失败:', error)
    petList.value = []
  }
}

// 获取传感器数据列表
const fetchSensorData = async () => {
  loading.value = true
  try {
    if (isPetOwner.value) {
      // 宠物主人：只获取自己宠物的数据
      const userPetIds = petList.value.map(pet => pet.petId || pet.id)

      if (userPetIds.length === 0) {
        tableData.value = []
        pageInfo.total = 0
        return
      }

      // 前端过滤逻辑（因为后端接口可能不支持 petIds 参数）
      let params = {
        ...searchForm,
        page: 1,
        size: 1000 // 获取较多数据进行前端过滤
      }

      const response = await getSensorData(params)
      if (response.code === 200) {
        let allRecords = response.data.records || []

        // 过滤
        const filteredRecords = allRecords.filter(record =>
            userPetIds.includes(record.petId || record.pet_id)
        )

        // 分页
        const total = filteredRecords.length
        const start = (pageInfo.page - 1) * pageInfo.size
        const end = start + pageInfo.size

        tableData.value = filteredRecords.slice(start, end)
        pageInfo.total = total
      }
    } else {
      // 管理员和兽医：获取所有数据
      const params = {
        ...searchForm,
        page: pageInfo.page,
        size: pageInfo.size
      }
      const response = await getSensorData(params)
      if (response.code === 200) {
        tableData.value = response.data.records || []
        pageInfo.total = response.data.total || 0
      }
    }
  } catch (error) {
    console.error('获取传感器数据失败:', error)
    ElMessage.error(error.message || '获取数据失败')
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
  fetchSensorData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    petId: null,
    minHeartRate: null,
    maxHeartRate: null,
    minTemperature: null,
    maxTemperature: null,
    minActivity: null,
    maxActivity: null,
    startTime: '',
    endTime: ''
  })
  timeRange.value = []
  pageInfo.page = 1
  fetchSensorData()
}

// 分页变化
const handleSizeChange = (size) => {
  pageInfo.size = size
  pageInfo.page = 1
  fetchSensorData()
}

const handleCurrentChange = (page) => {
  pageInfo.page = page
  fetchSensorData()
}

// 选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  // 新增时清空 activity，触发后端自动生成
  sensorDataForm.activity = null
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(sensorDataForm, {
    dataId: row.dataId,
    petId: row.petId,
    heartRate: row.heartRate,
    temperature: row.temperature,
    activity: row.activity,
    collectedAt: row.collectedAt
  })
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  viewData.value = { ...row }
  detailDialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await deleteSensorData(row.dataId)
    ElMessage.success('删除数据成功')
    fetchSensorData()
  } catch (error) {
    ElMessage.error(error.message || '删除数据失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (!isAdmin.value && !isVet.value) {
    ElMessage.error('您没有权限批量删除数据')
    return
  }

  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除选中的数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const dataIds = multipleSelection.value.map(item => item.dataId)
    await batchDeleteSensorData(dataIds)
    ElMessage.success('批量删除数据成功')
    fetchSensorData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除数据失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!sensorDataFormRef.value) return

  await sensorDataFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...sensorDataForm }
        delete data.dataId

        if (isEdit.value) {
          await updateSensorData(sensorDataForm.dataId, data)
          ElMessage.success('更新数据成功')
        } else {
          await createSensorData(data)
          ElMessage.success('创建数据成功 (活动量已自动生成)')
        }

        dialogVisible.value = false
        fetchSensorData()
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
  Object.assign(sensorDataForm, {
    dataId: null,
    petId: null,
    heartRate: null,
    temperature: null,
    activity: null,
    collectedAt: ''
  })
  if (sensorDataFormRef.value) {
    sensorDataFormRef.value.clearValidate()
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  await fetchPetList()
  fetchSensorData()
})
</script>

<style scoped lang="scss">
.sensor-data-management {
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

// 响应式设计
@media (max-width: 768px) {
  .sensor-data-management {
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