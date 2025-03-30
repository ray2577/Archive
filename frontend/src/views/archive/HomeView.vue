<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>档案统计</span>
            </div>
          </template>
          <div class="statistics">
            <div class="stat-item">
              <div class="stat-value">{{ totalArchives }}</div>
              <div class="stat-label">总档案数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayArchives }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
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
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const totalArchives = ref(1234)
const todayArchives = ref(56)

const recentActivities = ref([
  {
    content: '新建档案：2024年第一季度财务报告',
    time: '2024-03-04 14:30',
    type: 'primary'
  },
  {
    content: '更新档案：人事档案更新',
    time: '2024-03-04 11:20',
    type: 'success'
  },
  {
    content: '归档完成：项目结项报告',
    time: '2024-03-03 16:45',
    type: 'info'
  }
])
</script>

<style scoped>
.home {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  margin-top: 8px;
  color: #666;
}

.box-card {
  margin-bottom: 20px;
}
</style> 