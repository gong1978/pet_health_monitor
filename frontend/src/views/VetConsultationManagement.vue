<template>
  <div class="vet-consultation-management">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <span class="card-title">兽医咨询管理</span>
        </div>
      </template>

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
          <el-form-item v-if="isAdmin || isVet" label="回答兽医">
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

      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column v-if="isAdmin || isVet" type="selection" width="55" />
        <el-table-column prop="consultId" label="ID" width="80" />
        <el-table-column prop="petName" label="宠物" min-width="100">
          <template #default="{ row }">{{ row.petName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="提问人" min-width="100">
          <template #default="{ row }">{{ row.userName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="answered" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.answered ? 'success' : 'warning'">{{ row.answered ? '已回答' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="askedAt" label="提问时间" width="160" />
        <el-table-column prop="answeredByName" label="回答人" width="100">
          <template #default="{ row }">{{ row.answeredByName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="(isAdmin || isVet) && !row.answered" size="small" type="success" @click="handleAnswer(row)">回答</el-button>
            <el-button v-if="isAdmin || isVet" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-popconfirm v-if="isAdmin || isVet || isOwner(row)" title="确定删除?" @confirm="handleDelete(row)">
              <template #reference><el-button size="small" type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="pageInfo.page" v-model:page-size="pageInfo.size" :total="pageInfo.total" layout="total, prev, pager, next" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="consultFormRef" :model="consultForm" :rules="rules" label-width="100px">
        <el-form-item label="宠物" prop="petId">
          <el-select v-model="consultForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.petId" :label="pet.name" :value="pet.petId" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="isAdmin" label="提问用户">
          <el-select v-model="consultForm.userId" placeholder="请选择用户" style="width: 100%">
            <el-option v-for="user in userList" :key="user.userId" :label="user.fullName || user.username" :value="user.userId" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题描述" prop="question">
          <el-input v-model="consultForm.question" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>

        <template v-if="isEdit && (isAdmin || isVet)">
          <el-divider>回答区域</el-divider>
          <el-form-item label="回答内容">
            <el-input v-model="consultForm.answer" type="textarea" :rows="4" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="answerDialogVisible" title="回复咨询" width="500px">
      <div style="margin-bottom: 15px; padding: 10px; background: #f9f9f9; border-radius: 4px;">
        <p><strong>问题：</strong>{{ currentConsult.question }}</p>
      </div>
      <el-form>
        <el-form-item label="回复内容">
          <el-input v-model="answerContent" type="textarea" :rows="5" placeholder="请输入专业建议..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="answerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAnswer" :loading="submitting">提交回复</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="咨询详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="宠物">{{ viewConsult.petName }}</el-descriptions-item>
        <el-descriptions-item label="提问人">{{ viewConsult.userName }}</el-descriptions-item>
        <el-descriptions-item label="提问时间">{{ viewConsult.askedAt }}</el-descriptions-item>
        <el-descriptions-item label="问题内容">
          <div style="white-space: pre-wrap;">{{ viewConsult.question }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="回复状态">
          <el-tag :type="viewConsult.answered ? 'success' : 'warning'">{{ viewConsult.answered ? '已回答' : '待处理' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="回复人" v-if="viewConsult.answered">{{ viewConsult.answeredByName }}</el-descriptions-item>
        <el-descriptions-item label="回复内容" v-if="viewConsult.answered">
          <div style="white-space: pre-wrap;">{{ viewConsult.answer }}</div>
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
const viewConsult = ref({})

const searchForm = reactive({ petId: null, userId: null, answeredBy: null, answered: null, keyword: '' })
const pageInfo = reactive({ page: 1, size: 10, total: 0 })
const consultForm = reactive({ consultId: null, petId: null, userId: null, question: '', answer: '', answeredBy: null })

const rules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑咨询' : '发起咨询')
const isAdmin = computed(() => authStore.userInfo?.role === 1)
const isVet = computed(() => authStore.userInfo?.role === 2)
const isPetOwner = computed(() => !isAdmin.value && !isVet.value)

// 辅助函数：判断是否是自己的记录
const isOwner = (row) => {
  return authStore.userInfo?.userId === row.userId
}

// 核心修复：宠物列表加载逻辑
const fetchPetList = async () => {
  try {
    let res
    if (isPetOwner.value) {
      // 宠物主人只查自己的
      res = await getMyPets()
      if (res.code === 200) petList.value = res.data || []
    } else {
      // 管理员/兽医查所有
      res = await getPets({ page: 1, size: 1000 })
      if (res.code === 200) petList.value = res.data.records || []
    }
  } catch (e) {
    console.warn('加载宠物列表异常', e)
    petList.value = []
  }
}

// 核心修复：权限控制，非管理员/兽医不请求用户列表
const fetchUserList = async () => {
  if (!isAdmin.value && !isVet.value) return
  try {
    const res = await getUsers({ page: 1, size: 1000 })
    if (res.code === 200) userList.value = res.data.records
  } catch (e) { console.warn(e) }
}

const fetchVetList = async () => {
  if (!isAdmin.value && !isVet.value) return
  try {
    const res = await getUsers({ page: 1, size: 1000, role: 2 })
    if (res.code === 200) vetList.value = res.data.records
  } catch (e) { console.warn(e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    // 宠物主人：虽然前端会传参，但后端Controller已强制加了数据隔离，这里为了逻辑完整性保留传参
    // 注意：如果是宠物主人，getMyPets 获取到的是自己的宠物ID列表，可以用来辅助前端过滤（可选），
    // 但最安全的是依赖后端的过滤。
    const params = { ...searchForm, page: pageInfo.page, size: pageInfo.size }

    const res = await getVetConsultations(params)

    if (res.code === 200) {
      tableData.value = res.data.records
      pageInfo.total = res.data.total
    }
  } catch (e) {
    if (!e.message?.includes('404')) ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageInfo.page = 1; fetchData() }
const handleReset = () => { Object.assign(searchForm, { petId: null, userId: null, answered: null, keyword: '' }); handleSearch() }
const handleSizeChange = (val) => { pageInfo.size = val; fetchData() }
const handleCurrentChange = (val) => { pageInfo.page = val; fetchData() }
const handleSelectionChange = (val) => multipleSelection.value = val

const handleAdd = () => {
  isEdit.value = false
  Object.assign(consultForm, { consultId: null, petId: null, userId: authStore.userInfo.userId, question: '', answer: '', answeredBy: null })
  dialogVisible.value = true
}
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(consultForm, row)
  dialogVisible.value = true
}
const handleAnswer = (row) => {
  currentConsult.value = { ...row }
  answerContent.value = ''
  answerDialogVisible.value = true
}
const handleView = (row) => { viewConsult.value = { ...row }; detailDialogVisible.value = true }

const handleDelete = async (row) => {
  try { await deleteVetConsultation(row.consultId); ElMessage.success('删除成功'); fetchData() } catch(e){}
}
const handleBatchDelete = async () => {
  try {
    await batchDeleteVetConsultations(multipleSelection.value.map(i=>i.consultId))
    ElMessage.success('删除成功')
    fetchData()
  } catch(e){}
}

// 核心修复：提交逻辑增强判空
const handleSubmit = async () => {
  if (!consultFormRef.value) return
  await consultFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...consultForm }
        delete data.consultId

        let res
        if (isEdit.value) res = await updateVetConsultation(consultForm.consultId, data)
        else res = await createVetConsultation(data)

        // 只有 code=200 才算成功，否则视为失败（request.js 会弹窗）
        if (res && res.code === 200) {
          ElMessage.success('操作成功')
          dialogVisible.value = false
          fetchData()
        }
      } catch (e) {
        console.error('操作中断:', e)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleSubmitAnswer = async () => {
  if (!answerContent.value) return ElMessage.warning('请输入回复')
  submitting.value = true
  try {
    const res = await answerConsultation(currentConsult.value.consultId, answerContent.value)
    if (res && res.code === 200) {
      ElMessage.success('回复成功')
      answerDialogVisible.value = false
      fetchData()
    }
  } catch(e){} finally { submitting.value = false }
}

onMounted(async () => {
  // 1. 先加载宠物
  await fetchPetList()

  // 2. [关键修复] 只有管理员/兽医才去加载用户列表，普通用户(gyh)直接跳过！
  if (isAdmin.value || isVet.value) {
    Promise.all([fetchUserList(), fetchVetList()]).catch(console.warn)
  }

  // 3. 加载列表
  fetchData()
})
</script>

<style scoped>
.vet-consultation-management { padding: 20px; }
.page-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 18px;     /* 统一字体大小 */
      font-weight: 600;    /* 统一加粗 */
      color: #333;         /* 统一颜色 */
    }
  }
}
.search-form { background: #f8f9fa; padding: 20px; margin-bottom: 20px; border-radius: 4px; }
.table-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; }
.text-ellipsis { display: inline-block; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; vertical-align: middle; }
</style>