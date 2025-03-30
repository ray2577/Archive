<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 数据统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6" v-for="(card, index) in statisticsCards" :key="index">
        <el-card class="statistic-card" :body-style="{ padding: '20px' }">
          <div class="statistic-icon" :style="{ backgroundColor: card.color }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="statistic-info">
            <div class="statistic-value">{{ card.value }}</div>
            <div class="statistic-title">{{ card.title }}</div>
          </div>
          <div class="statistic-footer">
            <span class="trend" :class="card.trend > 0 ? 'up' : 'down'">
              <el-icon><component :is="card.trend > 0 ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
              {{ Math.abs(card.trend) }}%
            </span>
            <span class="period">较上月</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="dashboard-chart-row">
      <!-- 档案数量变化趋势图 -->
      <el-col :xs="24" :md="16">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>档案数量变化趋势</span>
              <el-radio-group v-model="trendTimeRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <div ref="trendChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 档案类型分布图 -->
      <el-col :xs="24" :md="8">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>档案类型分布</span>
              <el-button-group>
                <el-tooltip content="刷新数据">
                  <el-button type="primary" plain size="small" @click="loadTypeDistribution">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </el-tooltip>
              </el-button-group>
            </div>
          </template>
          <div class="chart-container">
            <div ref="typeChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="dashboard-table-row">
      <!-- 最近活动 -->
      <el-col :xs="24" :md="12">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button type="text" @click="loadRecentActivities">查看更多</el-button>
            </div>
          </template>
          <div class="activity-list">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in recentActivities"
                :key="index"
                :type="activity.type"
                :color="getActivityColor(activity.type)"
                :timestamp="activity.time"
                :hollow="activity.hollow"
              >
                {{ activity.content }}
                <div class="activity-user">{{ activity.user }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
      
      <!-- 借阅排行 -->
      <el-col :xs="24" :md="12">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>借阅排行</span>
              <el-button type="text" @click="navigateTo('/archive/list')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="borrowRanking" style="width: 100%" size="large">
            <el-table-column label="排名" width="60">
              <template #default="{ $index }">
                <div class="ranking-cell">
                  <span :class="['ranking-badge', $index < 3 ? `top-${$index + 1}` : '']">
                    {{ $index + 1 }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="档案名称" show-overflow-tooltip />
            <el-table-column prop="count" label="借阅次数" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ formatStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, TitleComponent,
  LegendComponent, ToolboxComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  Document, User, Timer, Collection,
  Refresh, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'

// 注册echarts组件
echarts.use([
  BarChart, LineChart, PieChart, GridComponent, TooltipComponent,
  TitleComponent, LegendComponent, ToolboxComponent, CanvasRenderer
])

const router = useRouter()

// 统计卡片数据
const statisticsCards = reactive([
  { 
    title: '档案总数', 
    value: '1,286', 
    icon: 'Document', 
    color: '#409EFF', 
    trend: 5.2 
  },
  { 
    title: '活跃用户', 
    value: '128', 
    icon: 'User', 
    color: '#67C23A', 
    trend: 8.3 
  },
  { 
    title: '本月新增', 
    value: '76', 
    icon: 'Timer', 
    color: '#E6A23C', 
    trend: -2.5 
  },
  { 
    title: '借阅记录', 
    value: '396', 
    icon: 'Collection', 
    color: '#F56C6C', 
    trend: 12.6 
  },
])

// 时间范围选择
const trendTimeRange = ref('month')

// 图表引用
const trendChartRef = ref(null)
const typeChartRef = ref(null)
const trendChart = ref(null)
const typeChart = ref(null)

// 最近活动
const recentActivities = ref([
  { 
    content: '上传了新档案《2023年第三季度财务报表》', 
    time: '2023-10-15 14:30', 
    user: '张经理', 
    type: 'success', 
    hollow: false 
  },
  { 
    content: '借阅了《市场调研报告2023》', 
    time: '2023-10-14 09:15', 
    user: '李研究员', 
    type: 'primary', 
    hollow: false 
  },
  { 
    content: '归还了《人事管理规定》', 
    time: '2023-10-13 16:45', 
    user: '王主管', 
    type: 'info', 
    hollow: false 
  },
  { 
    content: '修改了《公司章程》', 
    time: '2023-10-12 11:20', 
    user: '陈总监', 
    type: 'warning', 
    hollow: false 
  },
  { 
    content: '删除了过期档案《临时合同2022》', 
    time: '2023-10-10 10:05', 
    user: '周管理员', 
    type: 'danger', 
    hollow: false 
  }
])

// 借阅排行
const borrowRanking = ref([
  { 
    title: '公司战略规划2023-2025', 
    count: 56, 
    status: 'AVAILABLE' 
  },
  { 
    title: '市场调研报告2023', 
    count: 48, 
    status: 'BORROWED' 
  },
  { 
    title: '产品开发计划书', 
    count: 42, 
    status: 'AVAILABLE' 
  },
  { 
    title: '人力资源管理制度', 
    count: 39, 
    status: 'AVAILABLE' 
  },
  { 
    title: '财务审计报告2022', 
    count: 35, 
    status: 'PROCESSING' 
  }
])

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  // 如果图表已经存在，先销毁
  if (trendChart.value) {
    trendChart.value.dispose()
  }
  
  trendChart.value = echarts.init(trendChartRef.value)
  
  // 根据时间范围获取数据
  const { xData, seriesData } = getTrendData(trendTimeRange.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['新增档案', '借阅次数']
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
      data: xData
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增档案',
        type: 'line',
        data: seriesData.newArchives,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '借阅次数',
        type: 'line',
        data: seriesData.borrowCount,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#67C23A'
        },
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  
  trendChart.value.setOption(option)
}

// 初始化类型分布图
const initTypeChart = () => {
  if (!typeChartRef.value) return
  
  // 如果图表已经存在，先销毁
  if (typeChart.value) {
    typeChart.value.dispose()
  }
  
  typeChart.value = echarts.init(typeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['财务类', '人事类', '行政类', '技术类', '其他']
    },
    series: [
      {
        name: '档案类型',
        type: 'pie',
        radius: '55%',
        center: ['50%', '60%'],
        data: [
          { value: 335, name: '财务类' },
          { value: 310, name: '人事类' },
          { value: 234, name: '行政类' },
          { value: 135, name: '技术类' },
          { value: 148, name: '其他' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  typeChart.value.setOption(option)
}

// 获取趋势数据
const getTrendData = (timeRange) => {
  let xData = []
  let newArchives = []
  let borrowCount = []
  
  // 根据时间范围生成示例数据
  if (timeRange === 'week') {
    xData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    newArchives = [5, 8, 12, 4, 7, 2, 3]
    borrowCount = [10, 15, 20, 12, 18, 5, 8]
  } else if (timeRange === 'month') {
    xData = Array.from({length: 30}, (_, i) => `${i+1}日`)
    newArchives = Array.from({length: 30}, () => Math.floor(Math.random() * 15))
    borrowCount = Array.from({length: 30}, () => Math.floor(Math.random() * 30))
  } else {
    xData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    newArchives = [42, 38, 55, 48, 62, 58, 70, 75, 82, 76, 68, 72]
    borrowCount = [85, 76, 92, 88, 104, 112, 120, 136, 128, 115, 108, 96]
  }
  
  return {
    xData,
    seriesData: {
      newArchives,
      borrowCount
    }
  }
}

// 获取活动颜色
const getActivityColor = (type) => {
  const colorMap = {
    success: '#67C23A',
    primary: '#409EFF',
    info: '#909399',
    warning: '#E6A23C',
    danger: '#F56C6C'
  }
  return colorMap[type] || colorMap.info
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'BORROWED': 'warning',
    'PROCESSING': 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    'AVAILABLE': '可借阅',
    'BORROWED': '已借出',
    'PROCESSING': '处理中'
  }
  return statusMap[status] || status
}

// 加载类型分布数据
const loadTypeDistribution = () => {
  // 模拟加载数据
  setTimeout(() => {
    initTypeChart()
  }, 500)
}

// 加载最近活动
const loadRecentActivities = () => {
  // 可以跳转到活动页面或者加载更多活动
}

// 页面导航
const navigateTo = (path) => {
  router.push(path)
}

// 监听时间范围变化
watch(trendTimeRange, () => {
  initTrendChart()
})

// 窗口大小变化时重绘图表
const handleResize = () => {
  if (trendChart.value) {
    trendChart.value.resize()
  }
  if (typeChart.value) {
    typeChart.value.resize()
  }
}

// 组件挂载
onMounted(() => {
  // 初始化图表
  initTrendChart()
  initTypeChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载
const onUnmounted = () => {
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  if (trendChart.value) {
    trendChart.value.dispose()
  }
  if (typeChart.value) {
    typeChart.value.dispose()
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

/* 统计卡片 */
.statistic-card {
  margin-bottom: 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.statistic-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.statistic-card .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px !important;
}

.statistic-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  margin-right: 16px;
  color: white;
}

.statistic-info {
  flex: 1;
}

.statistic-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  color: #303133;
}

.statistic-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.statistic-footer {
  display: flex;
  align-items: center;
  margin-top: 12px;
  font-size: 12px;
}

.trend {
  display: flex;
  align-items: center;
  margin-right: 8px;
}

.trend.up {
  color: #67C23A;
}

.trend.down {
  color: #F56C6C;
}

.period {
  color: #909399;
}

/* 图表卡片 */
.dashboard-chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
}

.chart {
  height: 100%;
  width: 100%;
}

/* 表格卡片 */
.table-card {
  height: 450px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-list {
  height: 350px;
  overflow-y: auto;
  padding: 0 10px;
}

.activity-user {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 排行榜 */
.ranking-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.ranking-badge {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 14px;
}

.ranking-badge.top-1 {
  background-color: #FFD700;
  color: white;
}

.ranking-badge.top-2 {
  background-color: #C0C0C0;
  color: white;
}

.ranking-badge.top-3 {
  background-color: #CD7F32;
  color: white;
}
</style> 