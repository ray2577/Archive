<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-icon">
        <el-icon :size="64"><WarningFilled /></el-icon>
      </div>
      <h1 class="error-title">页面不存在</h1>
      <div class="error-desc">抱歉，您访问的页面不存在或已被删除</div>
      <div class="action-buttons">
        <el-button type="primary" @click="goBack">
          <el-icon><Back /></el-icon> 返回上一页
        </el-button>
        <el-button @click="goHome">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { WarningFilled, Back, HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
  max-width: 600px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1.2;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-icon {
  color: #E6A23C;
  margin-bottom: 20px;
}

.error-title {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}

.error-desc {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

@media (max-width: 768px) {
  .not-found-content {
    padding: 30px 20px;
  }
  
  .error-code {
    font-size: 80px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
}
</style> 