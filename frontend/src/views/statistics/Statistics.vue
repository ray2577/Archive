<template>
  <div class="statistics-container">
    <!-- 页面标题和时间范围选择 -->
    <div class="page-header">
      <h2>统计分析</h2>
      <div class="time-range">
        <el-radio-group v-model="timeRange" size="large" @change="handleTimeRangeChange">
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="quarter">本季度</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
        </el-radio-group>
        <el-button @click="refreshData" icon="Refresh" :loading="loading">刷新</el-button>
      </div>
    </div>
    
    <!-- 数据概览 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :xs="12" :sm="6" :md="6" :lg="6" v-for="(card, index) in statisticsCards" :key="index">
        <el-card class="statistic-card" :body-style="{ padding: 0 }" shadow="hover">
          <div class="card-content">
            <div class="statistic-icon" :style="{ backgroundColor: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="statistic-info">
              <div class="statistic-title">{{ card.title }}</div>
              <div class="statistic-value">{{ card.value }}</div>
              <div class="statistic-footer">
                <span class="trend" :class="card.trend > 0 ? 'up' : 'down'">
                  <el-icon><component :is="card.trend > 0 ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
                  {{ Math.abs(card.trend) }}%
                </span>
                <span class="period">较上{{ timeRange === 'week' ? '周' : timeRange === 'month' ? '月' : timeRange === 'quarter' ? '季' : '年' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 档案类型分布 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>档案类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="typeChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 档案状态分布 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>档案状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="statusChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 存储位置分布 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>存储位置分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="locationChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <!-- 档案新增趋势 -->
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>档案新增趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="trendChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 借阅量趋势 -->
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>借阅量趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="borrowChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <!-- 借阅排行榜 -->
      <el-col :xs="24" :md="12">
        <el-card class="rank-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>借阅次数排行</span>
              <el-button type="text" @click="exportRankingData">导出</el-button>
            </div>
          </template>
          <div class="ranking-list">
            <el-table :data="borrowRanking" style="width: 100%" size="large">
              <el-table-column label="排名" width="70">
                <template #default="{ $index }">
                  <div class="ranking-index">
                    <span v-if="$index < 3" class="top-rank">{{ $index + 1 }}</span>
                    <span v-else>{{ $index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="档案名称" show-overflow-tooltip />
              <el-table-column prop="count" label="借阅次数" width="100" />
            </el-table>
          </div>
        </el-card>
      </el-col>
      
      <!-- 热门关键词 -->
      <el-col :xs="24" :md="12">
        <el-card class="word-cloud-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>热门关键词</span>
            </div>
          </template>
          <div class="word-cloud-container">
            <div ref="wordCloudRef" class="word-cloud"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 用户活跃度区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>用户活跃度分析</span>
              <el-select v-model="userActivityType" @change="updateUserActivityChart" style="width: 120px">
                <el-option label="借阅量" value="borrow" />
                <el-option label="上传量" value="upload" />
                <el-option label="总活跃度" value="total" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div ref="userActivityChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, TitleComponent,
  LegendComponent, ToolboxComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { Document, Files, Folder, PieChart as PieChartIcon, DataLine, Timer, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import 'echarts-wordcloud'

// 注册echarts组件
echarts.use([
  PieChart, BarChart, LineChart, GridComponent, TooltipComponent,
  TitleComponent, LegendComponent, ToolboxComponent, CanvasRenderer
])

// 状态变量
const loading = ref(false)
const timeRange = ref('month')
const userActivityType = ref('borrow')

// 图表引用
const typeChartRef = ref(null)
const statusChartRef = ref(null)
const locationChartRef = ref(null)
const trendChartRef = ref(null)
const borrowChartRef = ref(null)
const wordCloudRef = ref(null)
const userActivityChartRef = ref(null)

// 图表实例
let typeChart = null
let statusChart = null
let locationChart = null
let trendChart = null
let borrowChart = null
let wordCloudChart = null
let userActivityChart = null

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
    title: '本期新增',
    value: '76',
    icon: 'Timer',
    color: '#67C23A',
    trend: 3.8
  },
  {
    title: '借阅次数',
    value: '396',
    icon: 'Files',
    color: '#E6A23C',
    trend: -2.5
  },
  {
    title: '用户活跃度',
    value: '85%',
    icon: 'DataLine',
    color: '#F56C6C',
    trend: 4.7
  }
])

// 借阅排行榜
const borrowRanking = ref([
  { title: '公司战略规划2023-2025', count: 56 },
  { title: '市场调研报告2023', count: 48 },
  { title: '产品开发计划书', count: 42 },
  { title: '人力资源管理制度', count: 39 },
  { title: '财务审计报告2022', count: 35 },
  { title: '员工手册V3.0', count: 28 },
  { title: '知识产权保护条例', count: 25 },
  { title: '客户满意度调查报告', count: 22 }
])

// 加载统计数据
const loadStatisticsData = async () => {
  loading.value = true
  
  try {
    // 在实际应用中，这里会调用API获取统计数据
    // 现在使用模拟数据
    setTimeout(() => {
      // 更新统计卡片数据
      updateStatisticsCards()
      
      // 初始化图表
      initCharts()
      
      loading.value = false
    }, 800)
  } catch (error) {
    console.error('Error loading statistics data:', error)
    ElMessage.error('加载统计数据失败，请稍后重试')
    loading.value = false
  }
}

// 更新统计卡片数据
const updateStatisticsCards = () => {
  // 模拟不同时间范围的数据变化
  const timeRangeFactors = {
    week: { baseline: 0.8, variance: 0.2 },
    month: { baseline: 1.0, variance: 0.1 },
    quarter: { baseline: 1.2, variance: 0.15 },
    year: { baseline: 1.5, variance: 0.3 }
  }
  
  const factor = timeRangeFactors[timeRange.value]
  
  statisticsCards[0].value = Math.floor(1286 * factor.baseline * (1 + (Math.random() - 0.5) * factor.variance)).toLocaleString()
  statisticsCards[1].value = Math.floor(76 * factor.baseline * (1 + (Math.random() - 0.5) * factor.variance)).toLocaleString()
  statisticsCards[2].value = Math.floor(396 * factor.baseline * (1 + (Math.random() - 0.5) * factor.variance)).toLocaleString()
  
  // 更新趋势数据
  statisticsCards.forEach(card => {
    card.trend = +(Math.random() * 15 - 5).toFixed(1)
  })
}

// 初始化所有图表
const initCharts = () => {
  nextTick(() => {
    initTypeChart()
    initStatusChart()
    initLocationChart()
    initTrendChart()
    initBorrowChart()
    initWordCloudChart()
    initUserActivityChart()
  })
}

// 初始化档案类型分布图表
const initTypeChart = () => {
  if (!typeChartRef.value) return
  
  // 如果图表已存在，销毁它
  if (typeChart) {
    typeChart.dispose()
  }
  
  typeChart = echarts.init(typeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
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
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 335, name: '财务类' },
          { value: 310, name: '人事类' },
          { value: 234, name: '行政类' },
          { value: 135, name: '技术类' },
          { value: 148, name: '其他' }
        ]
      }
    ]
  }
  
  typeChart.setOption(option)
}

// 初始化档案状态分布图表
const initStatusChart = () => {
  if (!statusChartRef.value) return
  
  // 如果图表已存在，销毁它
  if (statusChart) {
    statusChart.dispose()
  }
  
  statusChart = echarts.init(statusChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['可借阅', '已借出', '处理中']
    },
    series: [
      {
        name: '档案状态',
        type: 'pie',
        radius: '60%',
        center: ['55%', '50%'],
        data: [
          { value: 735, name: '可借阅', itemStyle: { color: '#67C23A' } },
          { value: 210, name: '已借出', itemStyle: { color: '#E6A23C' } },
          { value: 148, name: '处理中', itemStyle: { color: '#909399' } }
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
  
  statusChart.setOption(option)
}

// 初始化存储位置分布图表
const initLocationChart = () => {
  if (!locationChartRef.value) return
  
  // 如果图表已存在，销毁它
  if (locationChart) {
    locationChart.dispose()
  }
  
  locationChart = echarts.init(locationChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '存储位置',
        type: 'pie',
        radius: ['30%', '50%'],
        itemStyle: {
          borderRadius: 5,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          formatter: '{b}: {c} ({d}%)'
        },
        data: [
          { value: 450, name: 'A区' },
          { value: 330, name: 'B区' },
          { value: 260, name: 'C区' }
        ]
      }
    ]
  }
  
  locationChart.setOption(option)
}

// 初始化档案新增趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  // 如果图表已存在，销毁它
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  // 根据时间范围获取数据
  const { xData, yData } = getTrendData(timeRange.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
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
        data: yData,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(64, 158, 255, 0.5)'
            },
            {
              offset: 1,
              color: 'rgba(64, 158, 255, 0.1)'
            }
          ])
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 初始化借阅量趋势图表
const initBorrowChart = () => {
  if (!borrowChartRef.value) return
  
  // 如果图表已存在，销毁它
  if (borrowChart) {
    borrowChart.dispose()
  }
  
  borrowChart = echarts.init(borrowChartRef.value)
  
  // 根据时间范围获取数据
  const { xData } = getTrendData(timeRange.value)
  // 生成不同的借阅数据
  const yData = xData.map(() => Math.floor(Math.random() * 30 + 10))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '借阅量',
        type: 'bar',
        data: yData,
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  
  borrowChart.setOption(option)
}

// 初始化词云图
const initWordCloudChart = () => {
  if (!wordCloudRef.value) return
  
  // 如果图表已存在，销毁它
  if (wordCloudChart) {
    wordCloudChart.dispose()
  }
  
  wordCloudChart = echarts.init(wordCloudRef.value)
  
  // 词云数据
  const keywords = [
    { name: '财务报表', value: 95 },
    { name: '战略规划', value: 82 },
    { name: '人事档案', value: 78 },
    { name: '市场调研', value: 73 },
    { name: '产品开发', value: 68 },
    { name: '知识产权', value: 64 },
    { name: '员工手册', value: 60 },
    { name: '客户资料', value: 58 },
    { name: '合同文本', value: 54 },
    { name: '规章制度', value: 52 },
    { name: '研发计划', value: 50 },
    { name: '会议纪要', value: 48 },
    { name: '销售报告', value: 46 },
    { name: '项目方案', value: 44 },
    { name: '培训资料', value: 42 },
    { name: '技术文档', value: 40 },
    { name: '绩效考核', value: 38 },
    { name: '预算计划', value: 36 },
    { name: '内部通知', value: 34 },
    { name: '质量控制', value: 32 }
  ]
  
  const option = {
    tooltip: {
      show: true
    },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '100%',
      height: '100%',
      right: null,
      bottom: null,
      sizeRange: [12, 30],
      rotationRange: [-90, 90],
      rotationStep: 45,
      gridSize: 8,
      drawOutOfBound: false,
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: function () {
          return 'rgb(' + [
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160)
          ].join(',') + ')'
        }
      },
      emphasis: {
        focus: 'self',
        textStyle: {
          textShadowBlur: 10,
          textShadowColor: '#333'
        }
      },
      data: keywords
    }]
  }
  
  wordCloudChart.setOption(option)
}

// 初始化用户活跃度图表
const initUserActivityChart = () => {
  if (!userActivityChartRef.value) return
  
  // 如果图表已存在，销毁它
  if (userActivityChart) {
    userActivityChart.dispose()
  }
  
  userActivityChart = echarts.init(userActivityChartRef.value)
  
  // 获取用户活跃度数据
  const userData = getUserActivityData(userActivityType.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: userData.legend
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: userData.users
    },
    series: userData.series
  }
  
  userActivityChart.setOption(option)
}

// 更新用户活跃度图表
const updateUserActivityChart = () => {
  initUserActivityChart()
}

// 获取趋势数据
const getTrendData = (range) => {
  let xData = []
  let yData = []
  
  switch (range) {
    case 'week':
      xData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      break
    case 'month':
      xData = Array.from({length: 30}, (_, i) => `${i+1}日`)
      break
    case 'quarter':
      xData = Array.from({length: 12}, (_, i) => `第${i+1}周`)
      break
    case 'year':
      xData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
      break
    default:
      xData = Array.from({length: 30}, (_, i) => `${i+1}日`)
  }
  
  // 根据时间范围生成不同的数据模式
  if (range === 'week') {
    yData = [5, 8, 12, 4, 7, 2, 3]
  } else if (range === 'month') {
    yData = Array.from({length: xData.length}, () => Math.floor(Math.random() * 15 + 2))
  } else if (range === 'quarter') {
    yData = Array.from({length: xData.length}, () => Math.floor(Math.random() * 30 + 5))
  } else {
    // 年度数据 - 有季节性模式
    yData = [10, 8, 15, 20, 25, 30, 28, 25, 22, 18, 12, 16]
  }
  
  return { xData, yData }
}

// 获取用户活跃度数据
const getUserActivityData = (type) => {
  const users = ['张经理', '李研究员', '王主管', '赵工程师', '陈总监', '周管理员', '吴分析师', '郑主任']
  
  let legend = []
  let series = []
  
  if (type === 'borrow') {
    legend = ['借阅次数']
    series = [{
      name: '借阅次数',
      type: 'bar',
      data: [32, 28, 25, 18, 15, 12, 10, 8],
      itemStyle: {
        color: '#409EFF'
      }
    }]
  } else if (type === 'upload') {
    legend = ['上传次数']
    series = [{
      name: '上传次数',
      type: 'bar',
      data: [20, 16, 30, 22, 8, 18, 10, 5],
      itemStyle: {
        color: '#67C23A'
      }
    }]
  } else {
    legend = ['借阅次数', '上传次数', '查阅次数']
    series = [
      {
        name: '借阅次数',
        type: 'bar',
        stack: 'total',
        data: [32, 28, 25, 18, 15, 12, 10, 8],
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '上传次数',
        type: 'bar',
        stack: 'total',
        data: [20, 16, 30, 22, 8, 18, 10, 5],
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '查阅次数',
        type: 'bar',
        stack: 'total',
        data: [25, 32, 15, 28, 22, 24, 20, 18],
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }
  
  return { users, legend, series }
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  loadStatisticsData()
}

// 刷新数据
const refreshData = () => {
  loadStatisticsData()
}

// 导出排行榜数据
const exportRankingData = () => {
  ElMessage.success('排行榜数据导出中...')
  
  // 模拟导出完成
  setTimeout(() => {
    ElMessage.success('排行榜数据导出成功')
  }, 1000)
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  typeChart?.resize()
  statusChart?.resize()
  locationChart?.resize()
  trendChart?.resize()
  borrowChart?.resize()
  wordCloudChart?.resize()
  userActivityChart?.resize()
}

// 组件挂载
onMounted(() => {
  // 加载统计数据
  loadStatisticsData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载
onBeforeUnmount(() => {
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  typeChart?.dispose()
  statusChart?.dispose()
  locationChart?.dispose()
  trendChart?.dispose()
  borrowChart?.dispose()
  wordCloudChart?.dispose()
  userActivityChart?.dispose()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.data-overview {
  margin-bottom: 20px;
}

.statistic-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.statistic-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  padding: 20px;
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

.statistic-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.statistic-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.statistic-footer {
  font-size: 12px;
  display: flex;
  align-items: center;
}

.trend {
  display: flex;
  align-items: center;
  margin-right: 8px;
}

.up {
  color: #67C23A;
}

.down {
  color: #F56C6C;
}

.period {
  color: #909399;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 350px;
}

.rank-card, .word-cloud-card {
  margin-bottom: 20px;
  height: 350px;
}

.chart-header, .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container, .word-cloud-container {
  height: 282px;
}

.chart, .word-cloud {
  height: 100%;
  width: 100%;
}

.ranking-list {
  margin-top: 10px;
  height: 282px;
  overflow-y: auto;
}

.ranking-index {
  text-align: center;
}

.top-rank {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  background-color: #f5a623;
  color: white;
  font-weight: bold;
}

.top-rank:nth-child(1) {
  background-color: #FFD700;
}

.top-rank:nth-child(2) {
  background-color: #C0C0C0;
}

.top-rank:nth-child(3) {
  background-color: #CD7F32;
}
</style> 