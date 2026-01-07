<template>
  <div class="pet-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">宠物管理</span>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="宠物名字">
            <el-input v-model="searchForm.name" placeholder="请输入宠物名字" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="物种">
            <el-select v-model="searchForm.species" placeholder="请选择物种" clearable style="width: 120px">
              <el-option label="猫" value="猫" />
              <el-option label="狗" value="狗" />
              <el-option label="鼠" value="鼠" />
            </el-select>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" placeholder="请选择性别" clearable style="width: 120px">
              <el-option label="雄性" value="雄性" />
              <el-option label="雌性" value="雌性" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-header">
        <div class="left-panel">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增宠物</el-button>
          <el-button
              v-if="isAdmin || isVet"
              type="danger"
              :icon="Delete"
              @click="handleBatchDelete"
              :disabled="multipleSelection.length === 0"
          >
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
        <el-table-column prop="petId" label="ID" width="80" />
        <el-table-column prop="imageUrl" label="照片" width="100">
          <template #default="{ row }">
            <el-image
                v-if="row.imageUrl"
                :src="getImageUrl(row.imageUrl)"
                :preview-src-list="[getImageUrl(row.imageUrl)]"
                style="width: 50px; height: 50px; border-radius: 4px;"
                fit="cover"
                preview-teleported
            />
            <span v-else style="color: #999;">暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="宠物名字" min-width="120" />
        <el-table-column prop="species" label="物种" width="100" />
        <el-table-column prop="breed" label="品种" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.gender === '雄性' ? 'primary' : 'success'" size="small">
              {{ row.gender || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80">
          <template #default="{ row }">
            {{ row.age ? `${row.age}岁` : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重" width="100">
          <template #default="{ row }">
            {{ row.weight ? `${row.weight}kg` : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-popconfirm
                title="确定要删除这只宠物吗？"
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
            :total="pageInfo.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form :model="petForm" :rules="rules" ref="petFormRef" label-width="100px">
        <el-form-item label="宠物名字" prop="name">
          <el-input v-model="petForm.name" placeholder="请输入宠物名字" />
        </el-form-item>

        <el-form-item label="物种" prop="species">
          <el-select v-model="petForm.species" placeholder="请选择物种" style="width: 100%">
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
            <el-option label="鼠" value="鼠" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="品种" prop="breed">
          <el-input v-model="petForm.breed" placeholder="请输入品种（如：金毛、英短）" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="petForm.gender">
            <el-radio label="雄性">雄性</el-radio>
            <el-radio label="雌性">雌性</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="petForm.age" :min="0" :max="30" placeholder="岁" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number v-model="petForm.weight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="疫苗记录" prop="vaccineRecord">
          <el-input type="textarea" v-model="petForm.vaccineRecord" rows="3" placeholder="记录疫苗接种情况" />
        </el-form-item>

        <el-form-item label="宠物照片">
          <el-upload
              class="upload-demo"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
          >
            <div v-if="petForm.imageUrl" class="image-preview">
              <img :src="getImageUrl(petForm.imageUrl)" style="width: 100px; height: 100px; border-radius: 4px; object-fit: cover;" />
              <div class="image-overlay">
                <el-icon color="#fff" size="20"><Plus /></el-icon>
              </div>
            </div>
            <el-button v-else type="primary" size="small">点击上传照片</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传 jpg/png 文件，且不超过 10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="宠物详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="宠物名字">{{ viewPet.name }}</el-descriptions-item>
        <el-descriptions-item label="物种">{{ viewPet.species }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ viewPet.breed || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ viewPet.gender || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ viewPet.age ? `${viewPet.age}岁` : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ viewPet.weight ? `${viewPet.weight}kg` : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewPet.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="疫苗记录" :span="2">
          <div style="white-space: pre-wrap;">{{ viewPet.vaccineRecord || '暂无记录' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="宠物照片" :span="2" v-if="viewPet.imageUrl">
          <el-image
              :src="getImageUrl(viewPet.imageUrl)"
              style="width: 200px; height: 200px; border-radius: 4px;"
              fit="cover"
              :preview-src-list="[getImageUrl(viewPet.imageUrl)]"
              preview-teleported
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
// JS 部分保持不变，无需修改
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getPets, getPetById, createPet, updatePet, deletePet, batchDeletePets } from '@/api/pet'
import { getMyPets, addPetRelation } from '@/api/userPet'
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
const petFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  species: '',
  gender: ''
})

// 分页信息
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 宠物表单
const petForm = reactive({
  petId: null,
  name: '',
  species: '',
  breed: '',
  gender: '',
  age: null,
  weight: null,
  vaccineRecord: '',
  imageUrl: ''
})

// 查看宠物详情
const viewPet = ref({})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入宠物名字', trigger: 'blur' }
  ],
  species: [
    { required: true, message: '请选择物种', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑宠物' : '新增宠物')

// 用户角色判断
const isAdmin = computed(() => authStore.userInfo?.role === 1)
const isVet = computed(() => authStore.userInfo?.role === 2)
const isPetOwner = computed(() => !isAdmin.value && !isVet.value)

// 上传配置：使用相对路径，走前端代理，避免 404
const uploadAction = '/api/files/upload?type=pet'

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${authStore.token}`
  }
})

// 获取图片URL：显式拼接后端地址，处理路径前缀问题
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.startsWith('http')) return imageUrl
  // 后端 context-path 已移除，直接拼接端口
  return `http://localhost:8080${imageUrl}`
}

// 获取宠物列表
const fetchPets = async () => {
  loading.value = true
  try {
    let response

    if (isPetOwner.value) {
      // 宠物主人：获取自己的宠物列表
      response = await getMyPets()
      if (response.code === 200) {
        // 过滤搜索条件
        let filteredPets = response.data || []

        if (searchForm.name) {
          filteredPets = filteredPets.filter(pet =>
              pet.name && pet.name.includes(searchForm.name)
          )
        }
        if (searchForm.species) {
          filteredPets = filteredPets.filter(pet =>
              pet.species === searchForm.species
          )
        }
        if (searchForm.gender) {
          filteredPets = filteredPets.filter(pet =>
              pet.gender === searchForm.gender
          )
        }

        // 手动分页
        const total = filteredPets.length
        const start = (pageInfo.page - 1) * pageInfo.size
        const end = start + pageInfo.size
        const paginatedPets = filteredPets.slice(start, end)

        tableData.value = paginatedPets
        pageInfo.total = total
      }
    } else {
      // 管理员和兽医：获取所有宠物
      const params = {
        ...searchForm,
        page: pageInfo.page,
        size: pageInfo.size
      }
      response = await getPets(params)
      if (response.code === 200) {
        tableData.value = response.data.records
        pageInfo.total = response.data.total
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '获取宠物列表失败')
    tableData.value = []
    pageInfo.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageInfo.page = 1
  fetchPets()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    species: '',
    gender: ''
  })
  pageInfo.page = 1
  fetchPets()
}

// 分页变化
const handleSizeChange = (size) => {
  pageInfo.size = size
  pageInfo.page = 1
  fetchPets()
}

const handleCurrentChange = (page) => {
  pageInfo.page = page
  fetchPets()
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
  // 宠物主人只能编辑自己的宠物（通过数据隔离已保证）
  isEdit.value = true
  Object.assign(petForm, {
    petId: row.petId,
    name: row.name,
    species: row.species,
    breed: row.breed || '',
    gender: row.gender || '',
    age: row.age,
    weight: row.weight,
    vaccineRecord: row.vaccineRecord || '',
    imageUrl: row.imageUrl || ''
  })
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  viewPet.value = { ...row }
  detailDialogVisible.value = true
}

// 删除宠物
const handleDelete = async (row) => {
  // 宠物主人只能删除自己的宠物（通过数据隔离已保证）
  try {
    await deletePet(row.petId)
    ElMessage.success('删除宠物成功')
    fetchPets()
  } catch (error) {
    ElMessage.error(error.message || '删除宠物失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  // 权限检查
  if (!isAdmin.value && !isVet.value) {
    ElMessage.error('您没有权限批量删除宠物')
    return
  }

  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的宠物')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除选中的宠物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const petIds = multipleSelection.value.map(item => item.petId)
    await batchDeletePets(petIds)
    ElMessage.success('批量删除宠物成功')
    fetchPets()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除宠物失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!petFormRef.value) return

  await petFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...petForm }
        delete data.petId

        if (isEdit.value) {
          await updatePet(petForm.petId, data)
          ElMessage.success('更新宠物成功')
        } else {
          const response = await createPet(data)

          // 如果是宠物主人，创建宠物后自动添加关联
          if (isPetOwner.value && response.code === 200) {
            try {
              const newPetId = response.data.petId || response.data.id
              if (newPetId) {
                await addPetRelation(newPetId)
                ElMessage.success('添加宠物成功')
              } else {
                ElMessage.success('创建宠物成功')
              }
            } catch (relationError) {
              console.error('添加宠物关联失败:', relationError)
              ElMessage.warning('宠物创建成功，但添加关联失败，请稍后重试')
            }
          } else {
            ElMessage.success('创建宠物成功')
          }
        }

        dialogVisible.value = false
        fetchPets()
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
  Object.assign(petForm, {
    petId: null,
    name: '',
    species: '',
    breed: '',
    gender: '',
    age: null,
    weight: null,
    vaccineRecord: '',
    imageUrl: ''
  })
  if (petFormRef.value) {
    petFormRef.value.clearValidate()
  }
}

// 文件上传处理
const handleBeforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'].includes(file.type)

  if (!isValidType) {
    ElMessage.error('只支持 jpg、jpeg、png、gif 格式的图片文件')
    return false
  }
  // [核心修复] 前端限制放宽到 10MB，与后端匹配
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    // 兼容 {url: "..."} 对象或直接 url 字符串
    petForm.imageUrl = response.data.url || response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// [核心修复] 详细的上传错误处理
const handleUploadError = (err) => {
  console.error("上传错误:", err)
  if (err.message && err.message.includes('Network Error')) {
    ElMessage.error('连接服务器失败，请确保后端已启动(端口8080)')
  } else if (err.status === 404) {
    ElMessage.error('接口未找到(404): 请检查后端路径配置')
  } else if (err.status === 500) {
    ElMessage.error('服务器内部错误(500): 请检查后端日志(可能是文件大小超限)')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPets()
})
</script>

<style scoped lang="scss">
.pet-management {
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

.upload-demo {
  .image-preview {
    position: relative;
    display: inline-block;

    &:hover .image-overlay {
      opacity: 1;
    }

    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 4px;
    }
  }
}

.dialog-footer {
  text-align: right;
}

// 响应式设计
@media (max-width: 768px) {
  .pet-management {
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