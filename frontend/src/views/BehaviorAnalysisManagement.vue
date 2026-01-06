<template>
  <div class="behavior-analysis-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">行为识别管理</span>
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
          <el-form-item label="行为类型">
            <el-select
                v-model="searchForm.behaviorType"
                placeholder="请选择行为类型"
                clearable
                style="width: 150px"
            >
              <el-option label="睡觉" value="sleep" />
              <el-option label="吃饭" value="eat" />
              <el-option label="散步" value="walk" />
              <el-option label="玩耍" value="play" />
              <el-option label="休息" value="rest" />
              <el-option label="运动" value="exercise" />
            </el-select>
          </el-form-item>
          <el-form-item label="置信度">
            <el-input
                v-model.number="searchForm.minConfidence"
                placeholder="最小值"
                style="width: 80px"
                type="number"
                :min="0"
                :max="1"
                :step="0.1"
            />
            <span style="margin: 0 5px;">-</span>
            <el-input
                v-model.number="searchForm.maxConfidence"
                placeholder="最大值"
                style="width: 80px"
                type="number"
                :min="0"
                :max="1"
                :step="0.1"
            />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
                v-model="startTimeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                @change="handleStartTimeRangeChange"
                style="width: 350px"
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
            新增记录
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
        <el-table-column prop="behaviorId" label="记录ID" width="80" />
        <el-table-column label="抓拍" width="100">
          <template #default="{ row }">
            <el-image
                v-if="row.imageUrl"
                :src="getImageUrl(row.imageUrl)"
                :preview-src-list="[getImageUrl(row.imageUrl)]"
                style="width: 50px; height: 50px; border-radius: 4px;"
                fit="cover"
                preview-teleported
            >
              <template #error>
                <div class="image-slot" style="display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span v-else style="color:#ccc">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="petName" label="宠物名字" min-width="120" />
        <el-table-column prop="behaviorType" label="行为类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getBehaviorTagType(row.behaviorType)">
              {{ getBehaviorTypeName(row.behaviorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="{ row }">
            {{ row.endTime || '进行中' }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="持续时长" width="100">
          <template #default="{ row }">
            <span v-if="row.duration !== null && row.duration !== undefined">
              {{ row.duration }}分钟
            </span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="100">
          <template #default="{ row }">
            <el-progress
                v-if="row.confidence !== null && row.confidence !== undefined"
                :percentage="Math.round(row.confidence * 100)"
                :format="format => `${format}%`"
                :stroke-width="8"
                :color="getConfidenceColor(row.confidence)"
            />
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 6px;">
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" @click="handleView(row)">详情</el-button>
              <el-popconfirm
                  title="确定要删除这条记录吗？"
                  @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
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
        width="600px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="behaviorFormRef"
          :model="behaviorForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="行为抓拍" prop="imageUrl">
          <div class="upload-container">
            <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                name="file"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
            >
              <img v-if="behaviorForm.imageUrl" :src="getImageUrl(behaviorForm.imageUrl)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              <el-alert
                  v-if="!isEdit"
                  title="AI 识别提示: 上传图片后将自动识别行为"
                  type="info"
                  :closable="false"
                  show-icon
              >
                <template #default>
                  上传图片后，系统将自动识别行为（如文件名含"睡觉"则自动选择睡觉）。
                </template>
              </el-alert>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="宠物" prop="petId">
          <el-select v-model="behaviorForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option
                v-for="pet in petList"
                :key="pet.petId"
                :label="pet.name"
                :value="pet.petId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="行为类型" prop="behaviorType">
          <el-select v-model="behaviorForm.behaviorType" placeholder="等待AI识别或手动选择" style="width: 100%">
            <el-option label="睡觉" value="sleep" />
            <el-option label="吃饭" value="eat" />
            <el-option label="散步" value="walk" />
            <el-option label="玩耍" value="play" />
            <el-option label="休息" value="rest" />
            <el-option label="运动" value="exercise" />
          </el-select>
        </el-form-item>

        <el-form-item label="行为描述" prop="description">
          <el-input
              type="textarea"
              v-model="behaviorForm.description"
              rows="3"
              placeholder="AI 识别后会自动生成描述，也可手动输入"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
              v-model="behaviorForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间">
          <el-date-picker
              v-model="behaviorForm.endTime"
              type="datetime"
              placeholder="选择结束时间（可选）"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            留空表示行为正在进行中
          </div>
        </el-form-item>

        <el-form-item label="置信度">
          <div v-if="behaviorForm.confidence === null" style="display: flex; align-items: center; gap: 10px;">
            <el-tag type="success" effect="dark">AI 自动生成</el-tag>
            <span style="font-size: 12px; color: #999;">(后台将随机生成 85%~99% 的可信度)</span>
            <el-button type="primary" link @click="behaviorForm.confidence = 0.90">手动调整</el-button>
          </div>
          <div v-else style="display: flex; align-items: center; width: 100%; gap: 10px;">
            <el-slider
                v-model="behaviorForm.confidence"
                :min="0"
                :max="1"
                :step="0.01"
                style="flex: 1;"
                :format-tooltip="formatTooltip"
            />
            <span style="width: 40px;">{{ (behaviorForm.confidence * 100).toFixed(0) }}%</span>
            <el-button type="info" link @click="behaviorForm.confidence = null">重置为自动</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="行为记录详情" width="600px">
      <div style="text-align: center; margin-bottom: 20px;" v-if="viewBehavior.imageUrl">
        <el-image
            :src="getImageUrl(viewBehavior.imageUrl)"
            :preview-src-list="[getImageUrl(viewBehavior.imageUrl)]"
            style="max-width: 300px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);"
            preview-teleported
        />
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ viewBehavior.behaviorId }}</el-descriptions-item>
        <el-descriptions-item label="宠物名字">{{ viewBehavior.petName }}</el-descriptions-item>
        <el-descriptions-item label="行为类型">
          <el-tag :type="getBehaviorTagType(viewBehavior.behaviorType)">
            {{ getBehaviorTypeName(viewBehavior.behaviorType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="置信度">
          <div v-if="viewBehavior.confidence !== null && viewBehavior.confidence !== undefined">
            <el-progress
                :percentage="Math.round(viewBehavior.confidence * 100)"
                :format="format => `${format}%`"
                :stroke-width="8"
                :color="getConfidenceColor(viewBehavior.confidence)"
            />
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">{{ viewBehavior.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">{{ viewBehavior.endTime || '进行中' }}</el-descriptions-item>
        <el-descriptions-item label="持续时长" :span="2">
          {{ viewBehavior.duration !== null && viewBehavior.duration !== undefined ? `${viewBehavior.duration}分钟` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="详细描述" :span="2">
          {{ viewBehavior.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Picture } from '@element-plus/icons-vue'
import { getBehaviorAnalysis, createBehaviorAnalysis, updateBehaviorAnalysis, deleteBehaviorAnalysis, batchDeleteBehaviorAnalysis } from '@/api/behaviorAnalysis'
import { getPets } from '@/api/pet'
import { getMyPets } from '@/api/userPet'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const multipleSelection = ref([])
const behaviorFormRef = ref()
const petList = ref([])
const startTimeRange = ref([])

// 搜索表单
const searchForm = reactive({
  petId: null,
  behaviorType: '',
  minConfidence: null,
  maxConfidence: null,
  startTimeFrom: '',
  startTimeTo: '',
  endTimeFrom: '',
  endTimeTo: ''
})

// 分页信息
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 行为记录表单 - confidence 初始为 null
const behaviorForm = reactive({
  behaviorId: null,
  petId: null,
  behaviorType: '',
  startTime: '',
  endTime: '',
  confidence: null, // null 表示自动生成
  imageUrl: '',
  description: ''
})

// 查看记录详情
const viewBehavior = ref({})

// 表单验证规则
const rules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  behaviorType: [{ required: true, message: '请选择行为类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  imageUrl: [{ required: true, message: '请上传行为照片', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑记录' : '新增记录')

const isAdmin = computed(() => authStore.userInfo?.role === 1)
const isVet = computed(() => authStore.userInfo?.role === 2)
const isPetOwner = computed(() => {
  const role = authStore.userInfo?.role
  return role === 3 || role === 0 || (!isAdmin.value && !isVet.value)
})

// [核心修复] 配置上传请求头
const uploadHeaders = computed(() => {
  const token = authStore.token || localStorage.getItem('token')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
})

// [核心修复] 配置上传接口地址
const uploadUrl = 'http://localhost:8080/files/upload?type=behavior'

// [核心修复] 通用图片路径处理函数
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

// 映射表
const getBehaviorTypeName = (type) => {
  const typeMap = { 'sleep': '睡觉', 'eat': '吃饭', 'walk': '散步', 'play': '玩耍', 'rest': '休息', 'exercise': '运动' }
  return typeMap[type] || type
}

const getBehaviorTagType = (type) => {
  const typeMap = { 'sleep': 'info', 'eat': 'success', 'walk': 'primary', 'play': 'warning', 'rest': '', 'exercise': 'danger' }
  return typeMap[type] || ''
}

const getConfidenceColor = (confidence) => {
  if (confidence >= 0.8) return '#67c23a'
  if (confidence >= 0.6) return '#e6a23c'
  return '#f56c6c'
}

const formatTooltip = (val) => `${(val * 100).toFixed(0)}%`

const behaviorKeywords = {
  '睡觉': 'sleep', 'sleep': 'sleep',
  '吃饭': 'eat', 'eat': 'eat', 'food': 'eat',
  '散步': 'walk', 'walk': 'walk',
  '玩耍': 'play', 'play': 'play', 'game': 'play', '飞盘': 'play',
  '休息': 'rest', 'rest': 'rest',
  '运动': 'exercise', 'exercise': 'exercise', '跑': 'exercise'
}

const beforeUpload = (rawFile) => {
  const isImg = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png';
  if (!isImg) ElMessage.error('图片必须是 JPG 或 PNG 格式!')
  // 前端限制10MB，配合后端
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

/**
 * 核心修改：上传成功回调
 */
const handleUploadSuccess = (response) => {
  // 后端 Result.java 定义的成功 code 通常为 200
  if (response.code === 200 || response.code === 1) {
    const data = response.data

    // 1. 设置图片路径
    behaviorForm.imageUrl = data.url || data

    // 2. 识别文件名并匹配行为
    const rawResult = data.recognizedBehavior || ''
    let matchedType = ''
    for (const key in behaviorKeywords) {
      if (rawResult.includes(key)) {
        matchedType = behaviorKeywords[key]
        break
      }
    }

    if (matchedType) {
      behaviorForm.behaviorType = matchedType
      behaviorForm.description = `【AI智能识别】系统检测到画面中存在 [${getBehaviorTypeName(matchedType)}] 行为。`
      // 这里不强制设置 confidence，保持 null 让后端随机生成，或者保留用户之前的设置
      // behaviorForm.confidence = 0.95

      // 手动触发校验，清除红框提示
      nextTick(() => {
        if (behaviorFormRef.value) {
          behaviorFormRef.value.validateField(['behaviorType', 'imageUrl'])
        }
      })
      ElMessage.success(`AI 识别成功：${getBehaviorTypeName(matchedType)}`)
    } else {
      behaviorForm.description = `已上传抓拍：${rawResult}。请手动确认行为。`
      nextTick(() => {
        if (behaviorFormRef.value) behaviorFormRef.value.validateField('imageUrl')
      })
    }

    // 自动填充时间
    behaviorForm.startTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

/**
 * [核心修复] 上传失败回调 - 捕获文件过大等错误
 */
const handleUploadError = (error) => {
  console.error('上传失败详情:', error)
  let msg = '上传请求失败'

  if (error.status === 404) {
    msg = '后端接口未找到 (404)，请检查 FileController'
  } else if (error.status === 401) {
    msg = '登录已过期 (401)，请重新登录'
  } else if (error.status === 500) {
    // 可能是文件过大导致后端抛出 MaxUploadSizeExceededException
    msg = '服务器错误 (500)：可能是文件过大或路径错误，请查看后端日志'
  } else if (error.message && error.message.includes('Network Error')) {
    msg = '无法连接到服务器，请确保后端已启动 (端口8080)'
  }

  ElMessage.error(msg)
}

const fetchPetList = async () => {
  try {
    let response = isPetOwner.value ? await getMyPets() : await getPets({ page: 1, size: 1000 })
    if (response.code === 200) petList.value = isPetOwner.value ? response.data : response.data.records
  } catch (error) { petList.value = [] }
}

const fetchBehaviorAnalysis = async () => {
  loading.value = true
  try {
    const params = { ...searchForm, page: pageInfo.page, size: pageInfo.size }
    const response = await getBehaviorAnalysis(params)
    if (response.code === 200) {
      const records = response.data.records || []
      if (isPetOwner.value) {
        const userPetIds = petList.value.map(p => p.petId)
        tableData.value = records.filter(r => userPetIds.includes(r.petId))
      } else {
        tableData.value = records
      }
      pageInfo.total = response.data.total || 0
    }
  } finally { loading.value = false }
}

const handleStartTimeRangeChange = (value) => {
  searchForm.startTimeFrom = value ? value[0] : ''
  searchForm.startTimeTo = value ? value[1] : ''
}

const handleSearch = () => { pageInfo.page = 1; fetchBehaviorAnalysis() }

const handleReset = () => {
  Object.assign(searchForm, { petId: null, behaviorType: '', minConfidence: null, maxConfidence: null, startTimeFrom: '', startTimeTo: '' })
  startTimeRange.value = []
  handleSearch()
}

const handleSizeChange = (size) => { pageInfo.size = size; handleSearch() }
const handleCurrentChange = (page) => { pageInfo.page = page; fetchBehaviorAnalysis() }
const handleSelectionChange = (selection) => multipleSelection.value = selection

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(behaviorForm, {
    behaviorId: row.behaviorId, petId: row.petId, behaviorType: row.behaviorType,
    startTime: row.startTime, endTime: row.endTime || '',
    confidence: row.confidence, // 保持原值
    imageUrl: row.imageUrl || '', description: row.description || ''
  })
  dialogVisible.value = true
}

const handleView = (row) => { viewBehavior.value = { ...row }; detailDialogVisible.value = true }

const handleDelete = async (row) => {
  try {
    const res = await deleteBehaviorAnalysis(row.behaviorId)
    if (res.code === 200) { ElMessage.success('删除成功'); fetchBehaviorAnalysis() }
  } catch (e) { ElMessage.error('删除失败') }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除选中记录？', '警告', { type: 'warning' })
    const ids = multipleSelection.value.map(item => item.behaviorId)
    const res = await batchDeleteBehaviorAnalysis(ids)
    if (res.code === 200) { ElMessage.success('批量删除成功'); fetchBehaviorAnalysis() }
  } catch (e) {}
}

const handleSubmit = async () => {
  if (!behaviorFormRef.value) return
  await behaviorFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...behaviorForm }
        const res = isEdit.value ? await updateBehaviorAnalysis(data.behaviorId, data) : await createBehaviorAnalysis(data)
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          fetchBehaviorAnalysis()
        }
      } finally { submitting.value = false }
    }
  })
}

const resetForm = () => {
  // confidence 设为 null，触发 AI 随机
  Object.assign(behaviorForm, { behaviorId: null, petId: null, behaviorType: '', startTime: '', endTime: '', confidence: null, imageUrl: '', description: '' })
  if (behaviorFormRef.value) behaviorFormRef.value.clearValidate()
}

onMounted(async () => {
  await fetchPetList()
  fetchBehaviorAnalysis()
})
</script>

<style scoped lang="scss">
.behavior-analysis-management { padding: 20px; }
.page-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .card-title { font-size: 18px; font-weight: 600; color: #333; }
  }
}
.search-form { margin-bottom: 20px; padding: 20px; background-color: #f8f9fa; border-radius: 4px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; .left-panel .el-button { margin-right: 10px; } }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; }
.dialog-footer { text-align: right; }
.upload-container { display: flex; flex-direction: column; gap: 10px; }
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; position: relative; overflow: hidden;
  &:hover { border-color: var(--el-color-primary); }
}
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 120px; height: 120px; text-align: center; display: flex; justify-content: center; align-items: center; }
.avatar { width: 120px; height: 120px; display: block; object-fit: cover; }
.upload-tip { font-size: 12px; line-height: 1.4; }
</style>