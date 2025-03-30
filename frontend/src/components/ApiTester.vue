<template>
  <div class="api-tester">
    <h3>API 连接测试工具</h3>
    
    <div class="api-form">
      <el-form :model="apiForm" label-width="100px">
        <el-form-item label="请求URL">
          <el-input v-model="apiForm.url" placeholder="/api/your-endpoint">
            <template #prepend>
              <el-select v-model="apiForm.method" style="width: 100px">
                <el-option label="GET" value="get" />
                <el-option label="POST" value="post" />
                <el-option label="PUT" value="put" />
                <el-option label="DELETE" value="delete" />
              </el-select>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="请求参数" v-if="apiForm.method !== 'get'">
          <el-input
            v-model="apiForm.data"
            type="textarea"
            :rows="5"
            placeholder="请输入JSON格式数据"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="sendRequest" :loading="loading">
            发送请求
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="api-response" v-if="response">
      <h4>响应结果</h4>
      <div class="response-status">
        状态码: <el-tag :type="response.status === 200 ? 'success' : 'danger'">{{ response.status }}</el-tag>
      </div>
      <div class="response-time">
        响应时间: {{ response.time }}ms
      </div>
      <div class="response-headers">
        <el-collapse>
          <el-collapse-item title="响应头信息">
            <pre>{{ formatJSON(response.headers) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
      <div class="response-data">
        <el-tabs>
          <el-tab-pane label="格式化">
            <pre class="json-content">{{ formatJSON(response.data) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="原始">
            <pre class="json-content">{{ JSON.stringify(response.data) }}</pre>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <div class="api-error" v-if="error">
      <h4>请求错误</h4>
      <el-alert
        :title="error.message"
        type="error"
        :description="error.description"
        show-icon
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'

const apiForm = reactive({
  method: 'get',
  url: '/api/user/info',
  data: ''
})

const loading = ref(false)
const response = ref(null)
const error = ref(null)

// 发送请求
const sendRequest = async () => {
  loading.value = true
  error.value = null
  response.value = null
  
  try {
    let data = null
    if (apiForm.data && apiForm.method !== 'get') {
      try {
        data = JSON.parse(apiForm.data)
      } catch (e) {
        error.value = {
          message: 'JSON解析错误',
          description: e.message
        }
        loading.value = false
        return
      }
    }
    
    const startTime = Date.now()
    
    const config = {
      method: apiForm.method,
      url: apiForm.url,
      ...(apiForm.method === 'get' ? {} : { data })
    }
    
    const res = await axios(config)
    
    const endTime = Date.now()
    
    response.value = {
      status: res.status,
      statusText: res.statusText,
      headers: res.headers,
      data: res.data,
      time: endTime - startTime
    }
  } catch (e) {
    error.value = {
      message: e.message,
      description: e.response ? `状态码: ${e.response.status} - ${e.response.statusText}` : '无响应数据'
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  apiForm.method = 'get'
  apiForm.url = '/api/user/info'
  apiForm.data = ''
  response.value = null
  error.value = null
}

// 格式化JSON
const formatJSON = (data) => {
  try {
    return JSON.stringify(data, null, 2)
  } catch (e) {
    return data
  }
}
</script>

<style scoped>
.api-tester {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.api-form {
  margin-bottom: 20px;
}

.api-response, 
.api-error {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.response-status,
.response-time {
  margin-bottom: 10px;
}

.response-headers,
.response-data {
  margin-top: 10px;
}

.json-content {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: monospace;
}
</style> 