<template>
  <div class="api-test">
    <h3>API路径测试</h3>
    
    <div class="test-paths">
      <div v-for="(path, index) in testPaths" :key="index" class="test-path">
        <div class="path-label">{{ path.name }}</div>
        <el-button 
          :type="path.status === 200 ? 'success' : path.status === null ? 'default' : 'danger'"
          @click="testPath(path)"
          :loading="path.loading"
        >
          测试 {{ path.url }}
        </el-button>
        <div v-if="path.status !== null" class="path-result">
          状态: {{ path.status }} 
          <span v-if="path.message">- {{ path.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'

const testPaths = reactive([
  { name: '直接路径', url: '/user/login', status: null, loading: false, method: 'get' },
  { name: 'API路径', url: '/api/user/login', status: null, loading: false, method: 'get' },
  { name: 'Archive路径', url: '/archive/user/login', status: null, loading: false, method: 'get' },
  { name: 'Archive API路径', url: '/archive/api/user/login', status: null, loading: false, method: 'get' },
  { name: 'API V1路径', url: '/api/v1/user/login', status: null, loading: false, method: 'get' },
  { name: 'REST路径', url: '/rest/user/login', status: null, loading: false, method: 'get' },
  { name: 'Auth路径', url: '/auth/login', status: null, loading: false, method: 'get' },
  { name: '健康检查', url: '/actuator/health', status: null, loading: false, method: 'get' },
])

const testPath = async (path) => {
  path.loading = true
  path.status = null
  path.message = ''
  
  try {
    const response = await axios({
      method: path.method,
      url: path.url,
      timeout: 3000,
    })
    path.status = response.status
    path.message = '请求成功'
  } catch (error) {
    if (error.response) {
      path.status = error.response.status
      // 401或404可能意味着端点存在但需要鉴权
      path.message = error.response.status === 401 || error.response.status === 404 
        ? '端点可能存在但需要鉴权' 
        : error.message
    } else {
      path.status = 500
      path.message = error.message
    }
  } finally {
    path.loading = false
  }
}
</script>

<style>
.api-test {
  padding: 20px;
}

.test-paths {
  margin-top: 20px;
}

.test-path {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.path-label {
  width: 120px;
  text-align: right;
}

.path-result {
  margin-left: 10px;
}
</style> 