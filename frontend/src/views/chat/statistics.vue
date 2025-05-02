<template>
  <div class="statistics-container">
    <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span>AI助手使用统计</span>
          <el-button type="primary" size="small" @click="refreshData">刷新</el-button>
        </div>
      </template>
      
      <div v-if="isLoading" class="loading-container">
        <el-loading />
      </div>
      
      <div v-else class="statistics-content">
        <div class="stats-overview">
          <div class="stat-item">
            <div class="stat-title">总问题数</div>
            <div class="stat-value">{{ statistics.totalQueries || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-title">有帮助的回复</div>
            <div class="stat-value">{{ statistics.helpfulQueries || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-title">相关度评分</div>
            <div class="stat-value">{{ formatRelevanceScore(statistics.averageRelevance) }}</div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="chart-container" v-if="chartData.labels.length > 0">
          <div class="chart-title">问题类型分布</div>
          <div class="chart">
            <el-chart
              type="pie"
              :data="chartData"
              :options="chartOptions"
              height="300px"
            />
          </div>
        </div>
        
        <div v-else class="no-data">
          <p>暂无足够数据生成图表</p>
        </div>
        
        <el-divider />
        
        <div class="recent-queries">
          <div class="section-title">最近查询</div>
          <el-table :data="recentQueries" style="width: 100%">
            <el-table-column prop="query" label="问题" show-overflow-tooltip />
            <el-table-column prop="time" label="时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.time) }}
              </template>
            </el-table-column>
            <el-table-column prop="queryType" label="类型" width="120" />
            <el-table-column prop="isHelpful" label="有帮助" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.isHelpful === true" type="success">是</el-tag>
                <el-tag v-else-if="scope.row.isHelpful === false" type="danger">否</el-tag>
                <el-tag v-else type="info">未评价</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatStatistics, getChatHistory } from '@/api/chat'

const isLoading = ref(true)
const statistics = ref({})
const recentQueries = ref([])
const chartData = ref({
  labels: [],
  datasets: [
    {
      data: [],
      backgroundColor: [
        '#409EFF',
        '#67C23A',
        '#E6A23C',
        '#F56C6C',
        '#909399'
      ]
    }
  ]
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'right'
    }
  }
}

// Format time for display
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Format relevance score
const formatRelevanceScore = (score) => {
  if (score === null || score === undefined) return '暂无数据'
  return score.toFixed(2) + ' / 5'
}

// Load statistics data
const loadStatistics = async () => {
  isLoading.value = true
  
  try {
    // Get statistics
    const statsRes = await getChatStatistics()
    statistics.value = statsRes.data
    
    // Get recent queries
    const historyRes = await getChatHistory(10)
    recentQueries.value = historyRes.data.map(item => ({
      query: item.query,
      time: item.createTime,
      queryType: formatQueryType(item.queryType),
      isHelpful: item.isHelpful
    }))
    
    // Prepare chart data
    if (historyRes.data.length > 0) {
      // Count query types
      const queryTypes = {}
      historyRes.data.forEach(item => {
        const type = formatQueryType(item.queryType)
        queryTypes[type] = (queryTypes[type] || 0) + 1
      })
      
      // Set chart data
      chartData.value.labels = Object.keys(queryTypes)
      chartData.value.datasets[0].data = Object.values(queryTypes)
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

// Format query type for display
const formatQueryType = (type) => {
  const typeMap = {
    'SEARCH': '搜索',
    'QUESTION': '问答',
    'BORROW': '借阅',
    'STATISTICS': '统计'
  }
  return typeMap[type] || type
}

// Refresh data
const refreshData = () => {
  loadStatistics()
}

// Initialize
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-content {
  padding: 10px 0;
}

.stats-overview {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px 20px;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  margin: 20px 0;
}

.chart-title,
.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.chart {
  height: 300px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #909399;
}

.recent-queries {
  margin-top: 20px;
}
</style> 