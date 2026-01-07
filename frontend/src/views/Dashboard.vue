<template>
  <div class="dashboard-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h2>🐾 宠物监测</h2>
        </div>
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
        >
          <!-- 首页 - 所有角色都有 -->
          <el-menu-item index="1">
            <span>首页</span>
          </el-menu-item>

          <!-- 宠物管理 - 所有角色都有 -->
          <el-menu-item index="2">
            <span>宠物管理</span>
          </el-menu-item>

          <!-- 健康数据 - 所有角色都有 -->
          <el-menu-item index="3">
            <span>健康数据</span>
          </el-menu-item>

          <!-- 行为分析 - 所有角色都有 -->
          <el-menu-item index="4">
            <span>行为分析</span>
          </el-menu-item>

          <!-- 医疗记录 - 所有角色都有 -->
          <el-menu-item index="5">
            <span>医疗记录</span>
          </el-menu-item>

          <!-- 异常预警 - 管理员和兽医可见 -->
          <el-menu-item index="6" v-if="userInfo.role === 1 || userInfo.role === 2">
            <span>异常预警</span>
          </el-menu-item>

          <!-- 兽医咨询 - 所有角色都有 -->
          <el-menu-item index="7">
            <span>兽医咨询</span>
          </el-menu-item>

          <!-- 用户管理 - 只有管理员可见 -->
          <el-menu-item index="8" v-if="userInfo.role === 1">
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主容器 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <h3>智能宠物健康与行为监测系统</h3>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                {{ userInfo.username }}
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <div v-if="activeMenu === '1'" class="content-section">
            <!--            <h2>欢迎来到宠物健康监测系统</h2>-->
            <div class="welcome-info" v-if="dashboardStats.userName">
              <p>您好，{{ dashboardStats.userName }}！
                <span v-if="dashboardStats.userRole === 1">（管理员）</span>
                <span v-else-if="dashboardStats.userRole === 2">（兽医）</span>
                <span v-else>（宠物主人）</span>
              </p>
            </div>

            <el-row :gutter="20" class="mt-20" v-loading="statsLoading">
              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-number">{{ dashboardStats.petCount || 0 }}</div>
                    <div class="stat-label">
                      {{ dashboardStats.userRole === 1 || dashboardStats.userRole === 2 ? '所有宠物' : '我的宠物' }}
                    </div>
                  </div>
                  <div class="stat-icon pet-icon">🐕</div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card" :class="{ 'has-alert': dashboardStats.alertCount > 0 && dashboardStats.userRole !== 1 && dashboardStats.userRole !== 2 }">
                  <div class="stat-content">
                    <div class="stat-number">{{ dashboardStats.alertCount || 0 }}</div>
                    <div class="stat-label">健康告警</div>
                    <div v-if="dashboardStats.alertCount > 0 && dashboardStats.userRole !== 1 && dashboardStats.userRole !== 2" class="alert-badge">
                      有新预警
                    </div>
                  </div>
                  <div class="stat-icon alert-icon">⚠️</div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-number">{{ dashboardStats.healthReportCount || 0 }}</div>
                    <div class="stat-label">医疗记录</div>
                  </div>
                  <div class="stat-icon health-icon">📋</div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-number">{{ dashboardStats.behaviorAnalysisCount || 0 }}</div>
                    <div class="stat-label">行为数据</div>
                  </div>
                  <div class="stat-icon behavior-icon">📊</div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 异常预警图表展示 - 只有管理员和兽医可见 -->
            <template v-if="dashboardStats.userRole === 1 || dashboardStats.userRole === 2">
              <el-row :gutter="20" class="mt-20">
                <el-col :span="12">
                  <el-card>
                    <template #header>
                      <div class="card-header">
                        <span>📊 预警等级分布</span>
                      </div>
                    </template>
                    <div ref="alertLevelChart" class="chart-container" v-loading="chartLoading"></div>
                  </el-card>
                </el-col>

                <el-col :span="12">
                  <el-card>
                    <template #header>
                      <div class="card-header">
                        <span>⏰ 近7天预警趋势</span>
                      </div>
                    </template>
                    <div ref="alertTrendChart" class="chart-container" v-loading="chartLoading"></div>
                  </el-card>
                </el-col>
              </el-row>

              <!-- 最新预警列表 -->
              <el-card class="mt-20">
                <template #header>
                  <div class="card-header">
                    <span>🚨 最新预警</span>
                    <el-button type="primary" size="small" @click="activeMenu = '6'">查看全部</el-button>
                  </div>
                </template>
                <el-table :data="recentAlerts" v-loading="alertsLoading" style="width: 100%">
                  <el-table-column prop="petName" label="宠物名字" width="120" />
                  <el-table-column prop="alertType" label="预警类型" width="130">
                    <template #default="{ row }">
                      <el-tag :type="getAlertTypeTagType(row.alertType)">{{ getAlertTypeName(row.alertType) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="level" label="等级" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.level === 'critical' ? 'danger' : 'warning'">
                        {{ row.level === 'critical' ? '严重' : '警告' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="alertMessage" label="预警内容" min-width="200">
                    <template #default="{ row }">
                      <span class="alert-message">{{ row.alertMessage || '-' }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createdAt" label="时间" width="160" />
                  <el-table-column prop="isResolved" label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.isResolved ? 'success' : 'warning'">
                        {{ row.isResolved ? '已处理' : '待处理' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </template>

            <!-- 宠物主人专属内容 -->
            <template v-else>
              <!-- 我的预警通知 -->
              <el-card class="mt-20" v-if="!myAlertsLoading">
                <template #header>
                  <div class="card-header">
                    <span>🚨 我的预警通知</span>
                    <el-tag type="danger" size="small" v-if="unreadAlertCount > 0">
                      {{ unreadAlertCount }} 条未处理
                    </el-tag>
                    <el-tag type="success" size="small" v-else-if="myAlerts.length === 0">
                      暂无预警
                    </el-tag>
                  </div>
                </template>

                <!-- 有预警时显示预警列表 -->
                <div class="alert-notifications" v-if="myAlerts.length > 0">
                  <div
                      v-for="alert in myAlerts.slice(0, 3)"
                      :key="alert.alertId"
                      class="alert-notification-item"
                      :class="{ 'critical': alert.level === 'critical', 'resolved': alert.isResolved }"
                  >
                    <div class="alert-icon">
                      <span v-if="alert.level === 'critical'">🔴</span>
                      <span v-else-if="alert.isResolved">✅</span>
                      <span v-else>🟡</span>
                    </div>
                    <div class="alert-content">
                      <div class="alert-header">
                        <span class="pet-name">{{ alert.petName }}</span>
                        <el-tag :type="getAlertTypeTagType(alert.alertType)" size="small">
                          {{ getAlertTypeName(alert.alertType) }}
                        </el-tag>
                        <span class="alert-time">{{ formatTime(alert.createdAt) }}</span>
                      </div>
                      <div class="alert-message">{{ alert.alertMessage }}</div>
                      <div class="alert-status" v-if="alert.isResolved">
                        <el-tag type="success" size="small">已处理</el-tag>
                      </div>
                    </div>
                  </div>

                  <div class="view-more" v-if="myAlerts.length > 3">
                    <el-button type="text" @click="showAllAlertsDialog = true">
                      查看全部 {{ myAlerts.length }} 条预警 →
                    </el-button>
                  </div>
                </div>

                <!-- 无预警时的提示 -->
                <div v-else class="no-alerts-tip">
                  <el-empty description="目前暂无预警信息，您的宠物很健康！" :image-size="100">
                    <template #image>
                      <span style="font-size: 48px;">🐾</span>
                    </template>
                  </el-empty>
                </div>
              </el-card>

              <!-- 快捷操作 -->
              <el-card class="mt-20">
                <template #header>
                  <div class="card-header">
                    <span>🐕 我的宠物快捷操作</span>
                  </div>
                </template>
                <el-row :gutter="20">
                  <el-col :span="6">
                    <el-button type="primary" size="large" @click="activeMenu = '2'" class="quick-action-btn">
                      <div class="btn-content">
                        <div class="btn-icon">🐕</div>
                        <div class="btn-text">宠物管理</div>
                      </div>
                    </el-button>
                  </el-col>
                  <el-col :span="6">
                    <el-button type="success" size="large" @click="activeMenu = '5'" class="quick-action-btn">
                      <div class="btn-content">
                        <div class="btn-icon">📋</div>
                        <div class="btn-text">健康报告</div>
                      </div>
                    </el-button>
                  </el-col>
                  <el-col :span="6">
                    <el-button type="warning" size="large" @click="activeMenu = '4'" class="quick-action-btn">
                      <div class="btn-content">
                        <div class="btn-icon">📊</div>
                        <div class="btn-text">行为分析</div>
                      </div>
                    </el-button>
                  </el-col>
                  <el-col :span="6">
                    <el-button type="info" size="large" @click="activeMenu = '7'" class="quick-action-btn">
                      <div class="btn-content">
                        <div class="btn-icon">🩺</div>
                        <div class="btn-text">兽医咨询</div>
                      </div>
                    </el-button>
                  </el-col>
                </el-row>
              </el-card>

              <el-card class="mt-20">
                <template #header>
                  <div class="card-header">
                    <span>💡 使用提示</span>
                  </div>
                </template>
                <el-alert
                    title="宠物健康小贴士"
                    type="info"
                    description="定期为您的宠物进行健康检查，关注宠物的行为变化，及时发现健康问题。如有异常，请及时咨询专业兽医。"
                    show-icon
                    :closable="false"
                />
              </el-card>
            </template>
          </div>

          <div v-else-if="activeMenu === '2'" class="content-section">
            <PetManagement />
          </div>

          <div v-else-if="activeMenu === '3'" class="content-section">
            <SensorDataManagement />
          </div>

          <div v-else-if="activeMenu === '4'" class="content-section">
            <BehaviorAnalysisManagement />
          </div>

          <div v-else-if="activeMenu === '5'" class="content-section">
            <HealthReportManagement />
          </div>

          <div v-else-if="activeMenu === '6'" class="content-section">
            <AlertManagement />
          </div>

          <div v-else-if="activeMenu === '7'" class="content-section">
            <VetConsultationManagement />
          </div>

          <div v-else-if="activeMenu === '8'" class="content-section">
            <UserManagement />
          </div>

          <div v-else-if="activeMenu === 'profile'" class="content-section">
            <Profile />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>

  <!-- 宠物主人预警详情对话框 -->
  <el-dialog v-model="showAllAlertsDialog" title="我的预警通知" width="800px">
    <el-table :data="myAlerts" v-loading="myAlertsLoading" style="width: 100%">
      <el-table-column prop="petName" label="宠物名字" width="120" />
      <el-table-column prop="alertType" label="预警类型" width="130">
        <template #default="{ row }">
          <el-tag :type="getAlertTypeTagType(row.alertType)" size="small">{{ getAlertTypeName(row.alertType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="等级" width="80">
        <template #default="{ row }">
          <el-tag :type="row.level === 'critical' ? 'danger' : 'warning'" size="small">
            {{ row.level === 'critical' ? '严重' : '警告' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="alertMessage" label="预警内容" min-width="200">
        <template #default="{ row }">
          <span class="alert-message">{{ row.alertMessage || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column prop="isResolved" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isResolved ? 'success' : 'warning'" size="small">
            {{ row.isResolved ? '已处理' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="dialog-tip">
      <el-alert
          title="提示"
          description="如发现异常预警，建议及时通过兽医咨询功能联系专业兽医进行处理。"
          type="info"
          :closable="false"
          show-icon
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="showAllAlertsDialog = false">关闭</el-button>
        <el-button type="primary" @click="activeMenu = '7'; showAllAlertsDialog = false">咨询兽医</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { logout } from '@/api/auth'
import { ArrowDown } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'
import { getAlerts } from '@/api/alert'
import { getMyPets } from '@/api/userPet'
import { getPets } from '@/api/pet'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { nextTick } from 'vue'
import UserManagement from '@/views/UserManagement.vue'
import Profile from '@/views/Profile.vue'
import PetManagement from '@/views/PetManagement.vue'
import SensorDataManagement from '@/views/SensorDataManagement.vue'
import HealthReportManagement from '@/views/HealthReportManagement.vue'
import BehaviorAnalysisManagement from '@/views/BehaviorAnalysisManagement.vue'
import AlertManagement from '@/views/AlertManagement.vue'
import VetConsultationManagement from '@/views/VetConsultationManagement.vue'

const router = useRouter()
const authStore = useAuthStore()
const activeMenu = ref('1')
const userInfo = ref({})
const statsLoading = ref(false)
const chartLoading = ref(false)
const alertsLoading = ref(false)

// 图表引用
const alertLevelChart = ref()
const alertTrendChart = ref()

// 图表实例
let alertLevelChartInstance = null
let alertTrendChartInstance = null

// 数据
const dashboardStats = ref({
  petCount: 0,
  alertCount: 0,
  healthReportCount: 0,
  behaviorAnalysisCount: 0,
  userRole: null,
  userName: ''
})

const recentAlerts = ref([])
const myAlerts = ref([])
const myAlertsLoading = ref(false)
const showAllAlertsDialog = ref(false)
const myPets = ref([]) // 用户的宠物列表

// 计算未读预警数量
const unreadAlertCount = computed(() => {
  return myAlerts.value.filter(alert => !alert.isResolved).length
})

// 获取统计数据
const fetchDashboardStats = async () => {
  statsLoading.value = true
  try {
    const response = await getDashboardStats()
    if (response.code === 200) {
      dashboardStats.value = response.data
    } else {
      ElMessage.error('获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 获取最新预警数据（管理员和兽医）
const fetchRecentAlerts = async () => {
  alertsLoading.value = true
  try {
    const response = await getAlerts({ page: 1, size: 10 })
    if (response.code === 200) {
      recentAlerts.value = response.data.records
      // 获取数据后初始化图表
      await nextTick()
      initCharts()
    }
  } catch (error) {
    console.error('获取预警数据失败:', error)
  } finally {
    alertsLoading.value = false
  }
}

// 获取用户的宠物列表
const fetchMyPets = async () => {
  try {
    // 优先尝试用户宠物关联接口
    try {
      const response = await getMyPets()
      if (response.code === 200 && response.data && response.data.length > 0) {
        myPets.value = response.data
        console.log('通过用户宠物关联获取到宠物列表:', myPets.value)
        return
      }
    } catch (userPetError) {
      console.log('用户宠物关联接口失败，尝试使用备用方案:', userPetError.message)
    }

    // 备用方案：获取所有宠物，然后根据ownerId过滤（假设宠物表有ownerId字段）
    const currentUserId = userInfo.value.userId || userInfo.value.id
    if (currentUserId) {
      const petResponse = await getPets({ ownerId: currentUserId })
      if (petResponse.code === 200 && petResponse.data) {
        // 如果返回的是分页数据
        const pets = petResponse.data.records || petResponse.data
        myPets.value = pets
        console.log('通过宠物查询获取到宠物列表:', myPets.value)
      }
    }
  } catch (error) {
    console.error('获取我的宠物列表完全失败:', error)
    // 如果都失败了，暂时使用空数组，这样至少不会报错
    myPets.value = []
  }
}

const fetchMyAlerts = async () => {
  myAlertsLoading.value = true
  try {
    // 1. 显式定义参数，不给 petId 留坑
    const params = {
      page: 1,
      size: 50, // 查多一点
      isResolved: false
    }

    // 2. 万能清洗循环：把对象里所有是 "" 的 key 全部删掉
    Object.keys(params).forEach(key => {
      if (params[key] === '') {
        delete params[key]
      }
    })

    console.log('gyh 正在发送的最终请求参数:', params) // 调试用

    const response = await getAlerts(params)

    if (response.code === 200) {
      myAlerts.value = response.data.records
      console.log('获取到的数据条数:', myAlerts.value.length)
    }
  } catch (error) {
    console.error('获取我的预警数据失败:', error)
  } finally {
    myAlertsLoading.value = false
  }
}


// 初始化预警等级分布饼图
const initAlertLevelChart = () => {
  if (!alertLevelChart.value) return

  // 如果实例已存在，先销毁
  if (alertLevelChartInstance) {
    alertLevelChartInstance.dispose()
  }

  alertLevelChartInstance = echarts.init(alertLevelChart.value)
  const alertStats = getAlertLevelStats()

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '预警等级',
        type: 'pie',
        radius: '60%',
        data: alertStats,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ],
    color: ['#ff6b6b', '#ffa500', '#4ecdc4']
  }

  alertLevelChartInstance.setOption(option)
}

// 初始化预警趋势折线图
const initAlertTrendChart = () => {
  if (!alertTrendChart.value) return

  // 如果实例已存在，先销毁
  if (alertTrendChartInstance) {
    alertTrendChartInstance.dispose()
  }

  alertTrendChartInstance = echarts.init(alertTrendChart.value)

  // [核心修改] 使用后端返回的真实数据
  // 如果后端没返回数据（比如旧缓存），则使用空数组防止报错
  const dates = dashboardStats.value.trendDates || []
  const criticalData = dashboardStats.value.trendCritical || []
  const warningData = dashboardStats.value.trendWarning || []

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['严重预警', '普通预警'],
      right: '10%',
      top: '10%',
      orient: 'horizontal'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates // 使用真实日期
    },
    yAxis: {
      type: 'value',
      minInterval: 1 // 保证Y轴刻度为整数
    },
    series: [
      {
        name: '严重预警',
        type: 'line',
        stack: 'Total', // 如果不想堆叠，可以去掉这行
        data: criticalData, // 使用真实严重预警数据
        itemStyle: { color: '#ff6b6b' },
        areaStyle: { opacity: 0.1 } // 增加一点区域填充更好看
      },
      {
        name: '普通预警',
        type: 'line',
        stack: 'Total',
        data: warningData, // 使用真实普通预警数据
        itemStyle: { color: '#ffa500' },
        areaStyle: { opacity: 0.1 }
      }
    ]
  }

  alertTrendChartInstance.setOption(option)
}

// 初始化所有图表
const initCharts = () => {
  // 避免重复初始化
  if (chartLoading.value) return

  chartLoading.value = true
  try {
    // 延迟执行确保DOM已渲染
    setTimeout(() => {
      // 检查DOM元素是否存在
      if (alertLevelChart.value && alertTrendChart.value) {
        initAlertLevelChart()
        initAlertTrendChart()
      }
      chartLoading.value = false
    }, 100)
  } catch (error) {
    console.error('图表初始化失败:', error)
    chartLoading.value = false
  }
}

// 获取预警等级统计
const getAlertLevelStats = () => {
  // 不再依赖 recentAlerts (只含10条)，而是使用后端返回的全局统计
  // 如果 dashboardStats 还没加载完，就给 0
  const stats = dashboardStats.value || {}

  const criticalCount = stats.criticalAlertCount || 0
  const warningCount = stats.warningAlertCount || 0
  const resolvedCount = stats.resolvedAlertCount || 0

  return [
    { value: criticalCount, name: '严重预警' },
    { value: warningCount, name: '警告预警' },
    { value: resolvedCount, name: '已处理' }
  ]
}


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

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = Math.floor((now - date) / (1000 * 60)) // 分钟差

  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return `${Math.floor(diff / 1440)}天前`
}

// 窗口大小调整处理
const handleResize = () => {
  if (alertLevelChartInstance) {
    alertLevelChartInstance.resize()
  }
  if (alertTrendChartInstance) {
    alertTrendChartInstance.resize()
  }
}

onMounted(async () => {
  userInfo.value = authStore.userInfo
  fetchDashboardStats()

  // 根据角色获取不同的预警数据
  if (userInfo.value.role === 1 || userInfo.value.role === 2) {
    // 管理员和兽医获取全部预警数据
    fetchRecentAlerts()
  } else {
    // 宠物主人先获取宠物列表，再获取预警数据
    await fetchMyPets()
    fetchMyAlerts()
  }

  // 添加窗口大小调整监听
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  if (alertLevelChartInstance) {
    alertLevelChartInstance.dispose()
    alertLevelChartInstance = null
  }
  if (alertTrendChartInstance) {
    alertTrendChartInstance.dispose()
    alertTrendChartInstance = null
  }

  // 移除窗口大小调整监听
  window.removeEventListener('resize', handleResize)
})

// 监听菜单切换，当切换到首页时重新初始化图表
watch(activeMenu, (newValue) => {
  if (newValue === '1' && (userInfo.value.role === 1 || userInfo.value.role === 2)) {
    // 延迟执行，确保DOM已更新
    nextTick(() => {
      setTimeout(() => {
        // 确保有预警数据才初始化图表
        if (recentAlerts.value && recentAlerts.value.length > 0) {
          initCharts()
        }
      }, 300)
    })
  }
})

const getRoleName = (role) => {
  const roleMap = {
    1: '管理员',
    2: '兽医',
    3: '宠物主人',
  }
  return roleMap[role] || '未知'
}

const handleMenuSelect = (index) => {
  // 权限检查
  if (index === '6' && !(userInfo.value.role === 1 || userInfo.value.role === 2)) {
    ElMessage.warning('您没有权限访问异常预警管理')
    return
  }

  if (index === '8' && userInfo.value.role !== 1) {
    ElMessage.warning('您没有权限访问用户管理')
    return
  }

  activeMenu.value = index
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    activeMenu.value = 'profile'
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
        .then(async () => {
          try {
            await logout()
          } catch (error) {
            console.error('登出请求失败:', error)
          }
          authStore.clearAuth()
          ElMessage.success('已退出登录')
          router.push('/login')
        })
        .catch(() => {
          ElMessage.info('已取消退出')
        })
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;

  .el-container {
    height: 100vh;
  }
}

.sidebar {
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
  border-right: none;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  height: 100vh;
  min-height: 100vh;

  .logo {
    padding: 25px 20px;
    text-align: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);

    h2 {
      font-size: 20px;
      margin: 0;
      color: #fff;
      font-weight: 600;
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
    }
  }

  // 覆盖 Element Plus 菜单默认样式
  .el-menu-vertical-demo {
    border-right: none;
    background: transparent;

    .el-menu-item {
      color: rgba(255, 255, 255, 0.9);
      border-radius: 8px;
      margin: 4px 12px;
      transition: all 0.3s ease;

      &:hover {
        background-color: rgba(255, 255, 255, 0.15);
        color: #fff;
        transform: translateX(5px);
      }

      &.is-active {
        background-color: rgba(255, 255, 255, 0.2);
        color: #fff;
        font-weight: 600;
        border-radius: 8px;

        &::before {
          content: '';
          position: absolute;
          left: -12px;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 20px;
          background: #fff;
          border-radius: 2px;
        }
      }

      span {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;

  .header-left {
    h3 {
      margin: 0;
      font-size: 18px;
      color: #333;
    }
  }

  .header-right {
    .user-info {
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #333;

      &:hover {
        color: #66a6ff;
      }
    }
  }
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
}

.content-section {
  h2 {
    color: #333;
    margin-bottom: 20px;
  }
}

.welcome-info {
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;

  p {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }
}

.stat-card {
  height: 150px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;

  &:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  &.has-alert {
    border: 2px solid #ff4d4f;
    animation: pulse 2s infinite;

    .stat-number {
      color: #ff4d4f !important;
    }
  }

  .stat-content {
    text-align: center;
    z-index: 2;
    position: relative;

    .stat-number {
      font-size: 32px;
      font-weight: bold;
      color: #66a6ff;
      margin-bottom: 10px;
    }

    .stat-label {
      font-size: 14px;
      color: #666;
      font-weight: 500;
      margin-bottom: 5px;
    }

    .alert-badge {
      display: inline-block;
      background: #ff4d4f;
      color: white;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 12px;
      animation: blink 1.5s ease-in-out infinite alternate;
    }
  }

  .stat-icon {
    position: absolute;
    right: 15px;
    top: 15px;
    font-size: 24px;
    opacity: 0.8;
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 77, 79, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0);
  }
}

@keyframes blink {
  from {
    opacity: 0.5;
  }
  to {
    opacity: 1;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-20 {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.alert-message {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

// 响应式设计
@media (max-width: 1200px) {
  .chart-container {
    height: 250px;
  }
}

@media (max-width: 768px) {
  .chart-container {
    height: 200px;
  }

  .dashboard-container {
    .sidebar {
      width: 150px !important;
    }
  }

  .quick-action-btn {
    width: 100%;
    height: 120px;
  }
}

// 预警通知样式
.alert-notifications {
  .alert-notification-item {
    display: flex;
    align-items: flex-start;
    padding: 15px;
    margin-bottom: 10px;
    border-radius: 8px;
    border-left: 4px solid #f0f0f0;
    background-color: #fafafa;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transform: translateY(-1px);
    }

    &.critical {
      border-left-color: #ff4d4f;
      background-color: #fff2f0;
    }

    &.resolved {
      border-left-color: #52c41a;
      background-color: #f6ffed;
    }

    .alert-icon {
      margin-right: 12px;
      font-size: 20px;
      margin-top: 2px;
    }

    .alert-content {
      flex: 1;

      .alert-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .pet-name {
          font-weight: 600;
          color: #333;
        }

        .alert-time {
          font-size: 12px;
          color: #999;
          margin-left: auto;
        }
      }

      .alert-message {
        color: #666;
        font-size: 14px;
        line-height: 1.4;
        margin-bottom: 8px;
      }

      .alert-status {
        display: flex;
        justify-content: flex-end;
      }
    }
  }

  .view-more {
    text-align: center;
    padding: 10px;
    border-top: 1px solid #f0f0f0;
    margin-top: 10px;
  }
}

.dialog-tip {
  margin-top: 20px;
}

.no-alerts-tip {
  padding: 40px 20px;
  text-align: center;
}

// 快捷操作按钮样式
.quick-action-btn {
  width: 100%;
  height: 120px;
  padding: 0;
  border-radius: 12px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  }

  .btn-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;

    .btn-icon {
      font-size: 36px;
      margin-bottom: 8px;
    }

    .btn-text {
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>