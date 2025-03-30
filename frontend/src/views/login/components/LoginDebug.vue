<template>
  <div class="login-debug">
    <el-collapse v-model="activeNames">
      <el-collapse-item title="登录调试工具" name="1">
        <div class="debug-actions">
          <el-button @click="testAllUrls" type="primary" :loading="testing">
            测试所有登录路径
          </el-button>
          <el-button @click="reset" type="info">重置结果</el-button>
        </div>
        
        <el-table :data="urlTests" style="width: 100%" v-loading="testing">
          <el-table-column prop="url" label="登录路径" width="220">
            <template #default="scope">
              <el-tooltip :content="scope.row.url" placement="top">
                <div class="url-cell">{{ scope.row.url }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag 
                :type="scope.row.status === 200 ? 'success' : scope.row.status ? 'danger' : 'info'"
              >
                {{ scope.row.status || '未测试' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="结果">
            <template #default="scope">
              <div class="result-cell">{{ scope.row.message || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button 
                @click="testUrl(scope.row)" 
                :disabled="testing" 
                type="primary" 
                size="small"
                :loading="scope.row.loading"
              >
                测试
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'

const activeNames = ref([])
const testing = ref(false)

// 定义所有可能的登录URL
const urlTests = reactive([
  { url: '/api/user/login', method: 'post', status: null, message: '', loading: false },
  { url: '/user/login', method: 'post', status: null, message: '', loading: false },
  { url: '/api/login', method: 'post', status: null, message: '', loading: false },
  { url: '/login', method: 'post', status: null, message: '', loading: false },
  { url: '/api/auth/login', method: 'post', status: null, message: '', loading: false },
  { url: '/auth/login', method: 'post', status: null, message: '', loading: false },
  { url: '/archive/api/user/login', method: 'post', status: null, message: '', loading: false },
  { url: '/archive/user/login', method: 'post', status: null, message: '', loading: false },
  { url: '/archive/login', method: 'post', status: null, message: '', loading: false },
  { url: '/archive/auth/login', method: 'post', status: null, message: '', loading: false }
])

// 测试单个URL
const testUrl = async (urlTest) => {
  urlTest.loading = true
  urlTest.status = null
  urlTest.message = ''
  
  const testData = {
    username: 'admin',
    password: '123456'
  }
  
  try {
    const response = await axios({
      method: urlTest.method,
      url: urlTest.url,
      data: testData,
      timeout: 5000
    })
    
    urlTest.status = response.status
    urlTest.message = '请求成功！路径可用'
    
    // 如果响应包含token，说明登录成功
    if (response.data && (response.data.token || (response.data.data && response.data.data.token))) {
      urlTest.message = '登录成功！获取到token！'
      return true
    }
  } catch (error) {
    if (error.response) {
      urlTest.status = error.response.status
      
      // 特殊处理一些状态码
      if (error.response.status === 401) {
        urlTest.message = '认证失败，路径正确但凭据错误'
        return true // 路径正确，只是凭据问题
      } else if (error.response.status === 400) {
        urlTest.message = '请求格式错误，但路径可能正确'
        return true // 路径可能正确，请求格式问题
      } else {
        urlTest.message = error.response.data?.message || error.message
      }
    } else {
      urlTest.status = 0
      urlTest.message = error.message
    }
    return false
  } finally {
    urlTest.loading = false
  }
}

// 测试所有URL
const testAllUrls = async () => {
  activeNames.value = ['1'] // 展开面板
  testing.value = true
  
  try {
    for (const urlTest of urlTests) {
      const success = await testUrl(urlTest)
      if (success) break // 如果找到可用路径，停止测试
    }
  } finally {
    testing.value = false
  }
}

// 重置测试结果
const reset = () => {
  urlTests.forEach(test => {
    test.status = null
    test.message = ''
    test.loading = false
  })
}
</script>

<style scoped>
.login-debug {
  margin-top: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.debug-actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.url-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.result-cell {
  white-space: normal;
  word-break: break-word;
}
</style> 