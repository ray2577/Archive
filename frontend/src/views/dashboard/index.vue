<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statCards" :key="index">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ item.value }}</div>
              <div class="card-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 最近活动 -->
      <el-col :xs="24" :sm="24" :md="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button type="text">查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :timestamp="activity.time"
              :type="activity.type"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button 
              v-for="(action, index) in quickActions" 
              :key="index"
              :type="action.type"
              :icon="action.icon"
              @click="handleQuickAction(action)"
            >
              {{ action.name }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Document, FolderAdd, Search, DataAnalysis, 
  User, Setting, ChatDotRound
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计卡片数据
const statCards = ref([
  { title: '档案总数', value: 1258, icon: 'Document', color: '#409EFF' },
  { title: '借阅中', value: 32, icon: 'Files', color: '#67C23A' },
  { title: '本月新增', value: 87, icon: 'Plus', color: '#E6A23C' },
  { title: '用户数', value: 45, icon: 'User', color: '#F56C6C' }
])

// 最近活动
const recentActivities = ref([
  { content: '张三借阅了《2023年财务报表》', time: '2023-05-15 14:30', type: 'primary' },
  { content: '李四归还了《人事档案》', time: '2023-05-14 10:15', type: 'success' },
  { content: '系统管理员更新了权限设置', time: '2023-05-13 16:45', type: 'warning' },
  { content: '新增档案《2023年第二季度报告》', time: '2023-05-12 09:30', type: 'info' }
])

// 快捷操作
const quickActions = ref([
  { name: '新增档案', icon: 'FolderAdd', type: 'primary', path: '/archive/list' },
  { name: '档案查询', icon: 'Search', type: 'success', path: '/archive/search' },
  { name: '借阅管理', icon: 'Document', type: 'warning', path: '/archive/borrow' },
  { name: '数据统计', icon: 'DataAnalysis', type: 'info', path: '/statistics' },
  { name: 'AI助手', icon: 'ChatDotRound', type: 'danger', path: '/ai-assistant' }
])

// 处理快捷操作
const handleQuickAction = (action) => {
  router.push(action.path)
}

// 获取仪表盘数据
const fetchDashboardData = () => {
  // 这里应该调用API获取实际数据
  console.log('获取仪表盘数据')
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  margin-right: 16px;
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.card-title {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-20 {
  margin-top: 20px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-actions .el-button {
  justify-content: flex-start;
}
</style> 