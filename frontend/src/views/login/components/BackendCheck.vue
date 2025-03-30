<template>
  <div class="backend-check">
    <h3>后端服务检查</h3>
    
    <div class="check-actions">
      <el-button @click="checkBackend" type="primary" :loading="checking">检查后端连接</el-button>
    </div>
    
    <div v-if="checkResult" class="check-result">
      <el-alert
        :title="checkResult.success ? '后端服务正常' : '后端服务异常'"
        :type="checkResult.success ? 'success' : 'error'"
        :description="checkResult.message"
        show-icon
      />
      
      <div v-if="checkResult.details" class="details">
        <h4>检查详情:</h4>
        <pre>{{ JSON.stringify(checkResult.details, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const checking = ref(false);
const checkResult = ref(null);

const checkBackend = async () => {
  checking.value = true;
  checkResult.value = null;
  
  try {
    // 首先检查后端是否运行
    const result = { success: false, tests: [] };
    
    // 1. 检查基本连接 - 尝试不同的根路径
    const rootPaths = [
      { name: '根路径 (GET /)' },
      { name: '健康检查 (GET /actuator/health)' },
      { name: 'Swagger接口文档 (GET /swagger-ui.html)' },
      { name: 'Swagger API (GET /v3/api-docs)' }
    ];
    
    for (const path of rootPaths) {
      try {
        const response = await axios.get(path.name.split('(')[1].split(')')[0].split(' ')[1]);
        path.status = response.status;
        path.success = true;
        path.message = '连接成功';
        result.tests.push(path);
      } catch (error) {
        path.status = error.response?.status || 0;
        path.success = false;
        path.message = error.message;
        result.tests.push(path);
      }
    }
    
    // 如果至少有一个测试成功，说明后端服务在运行
    const backendRunning = result.tests.some(test => test.success);
    
    if (backendRunning) {
      checkResult.value = {
        success: true,
        message: '后端服务正在运行，但可能使用了不同的API路径结构。',
        details: result
      };
    } else {
      checkResult.value = {
        success: false,
        message: '无法连接到后端服务，请确认后端服务已启动并运行在正确的端口上。',
        details: result
      };
    }
  } catch (error) {
    checkResult.value = {
      success: false,
      message: `检查过程中出错: ${error.message}`,
      details: { error: error.message }
    };
  } finally {
    checking.value = false;
  }
};
</script>

<style scoped>
.backend-check {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.check-actions {
  margin-bottom: 20px;
}

.check-result {
  margin-top: 20px;
}

.details {
  margin-top: 15px;
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.details pre {
  white-space: pre-wrap;
  font-family: monospace;
}
</style> 