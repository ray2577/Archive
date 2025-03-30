<template>
  <div class="api-debug-container">
    <div class="page-header">
      <h2>API 调试</h2>
    </div>
    
    <el-tabs>
      <el-tab-pane label="API测试">
        <ApiTester />
      </el-tab-pane>
      
      <el-tab-pane label="环境信息">
        <el-card>
          <h3>环境变量</h3>
          <el-descriptions border>
            <el-descriptions-item label="BASE_API">
              {{ import.meta.env.VITE_BASE_API }}
            </el-descriptions-item>
            <el-descriptions-item label="API_TIMEOUT">
              {{ import.meta.env.VITE_API_TIMEOUT }}
            </el-descriptions-item>
            <el-descriptions-item label="环境">
              {{ import.meta.env.MODE }}
            </el-descriptions-item>
            <el-descriptions-item label="开发模式">
              {{ import.meta.env.DEV ? '是' : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="生产模式">
              {{ import.meta.env.PROD ? '是' : '否' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <h3 class="mt-4">请求配置</h3>
          <el-descriptions border>
            <el-descriptions-item label="Proxy">
              /api → http://localhost:8080/archive
            </el-descriptions-item>
            <el-descriptions-item label="Mock数据">
              {{ useMockData ? '启用' : '禁用' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="网络检测">
        <el-card>
          <h3>网络状态检测</h3>
          
          <div class="network-check">
            <div class="check-item">
              <span class="check-label">前端服务:</span>
              <el-tag type="success">正常</el-tag>
            </div>
            
            <div class="check-item">
              <span class="check-label">后端连接:</span>
              <el-tag :type="backendConnected ? 'success' : 'danger'">
                {{ backendConnected ? '正常' : '连接失败' }}
              </el-tag>
              <el-button size="small" @click="checkBackendConnection" :loading="checkingConnection">
                检测
              </el-button>
            </div>
            
            <div class="check-item">
              <span class="check-label">互联网连接:</span>
              <el-tag :type="internetConnected ? 'success' : 'danger'">
                {{ internetConnected ? '正常' : '连接失败' }}
              </el-tag>
              <el-button size="small" @click="checkInternetConnection" :loading="checkingInternet">
                检测
              </el-button>
            </div>
          </div>
          
          <div v-if="connectionDetails" class="connection-details">
            <h4>连接详情</h4>
            <pre>{{ connectionDetails }}</pre>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ApiTester from '@/components/ApiTester.vue'
import axios from 'axios'

// 模拟数据状态
const useMockData = computed(() => {
  return process.env.NODE_ENV === 'development' && window.handleMockResponse !== undefined
})

// 连接状态
const backendConnected = ref(false)
const internetConnected = ref(false)
const checkingConnection = ref(false)
const checkingInternet = ref(false)
const connectionDetails = ref('')

// 检查后端连接
const checkBackendConnection = async () => {
  checkingConnection.value = true
  connectionDetails.value = ''
  
  try {
    const startTime = Date.now()
    const response = await axios.get('/api/health', { timeout: 5000 })
    const endTime = Date.now()
    
    backendConnected.value = response.status === 200
    
    connectionDetails.value = `
状态码: ${response.status}
响应时间: ${endTime - startTime}ms
响应数据: ${JSON.stringify(response.data, null, 2)}
    `.trim()
  } catch (error) {
    backendConnected.value = false
    connectionDetails.value = `
错误: ${error.message}
${error.response ? `状态码: ${error.response.status}` : '无响应'}
    `.trim()
    
    // 尝试不同的端点
    try {
      const res = await axios.get('/api/user/info', { timeout: 5000 })
      if (res.status === 200) {
        backendConnected.value = true
        connectionDetails.value += `\n\n备用端点(/api/user/info)连接成功!`
      }
    } catch (e) {
      connectionDetails.value += `\n\n备用端点(/api/user/info)也连接失败: ${e.message}`
    }
  } finally {
    checkingConnection.value = false
  }
}

// 检查互联网连接
const checkInternetConnection = async () => {
  checkingInternet.value = true
  
  try {
    const response = await axios.get('https://www.baidu.com', { timeout: 5000 })
    internetConnected.value = response.status === 200
  } catch (error) {
    internetConnected.value = false
  } finally {
    checkingInternet.value = false
  }
}

// 初始检查
checkBackendConnection()
checkInternetConnection()
</script>

<style scoped>
.api-debug-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.mt-4 {
  margin-top: 20px;
}

.network-check {
  margin: 20px 0;
}

.check-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.check-label {
  width: 100px;
  font-weight: 500;
}

.check-item .el-tag {
  margin: 0 10px;
}

.connection-details {
  margin-top: 20px;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.connection-details pre {
  margin: 0;
  font-family: monospace;
  white-space: pre-wrap;
}
</style> 