<template>
  <div class="login-form-debug">
    <h3>登录表单调试</h3>
    
    <el-divider />
    
    <el-form :model="form" label-width="120px">
      <el-form-item label="登录路径">
        <el-select v-model="form.url" placeholder="选择登录路径">
          <el-option
            v-for="option in urlOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="请求格式">
        <el-select v-model="form.format" placeholder="选择请求格式">
          <el-option label="JSON" value="json" />
          <el-option label="表单数据" value="form" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="用户名字段">
        <el-input v-model="form.usernameField" />
      </el-form-item>
      
      <el-form-item label="密码字段">
        <el-input v-model="form.passwordField" />
      </el-form-item>
      
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="testLogin" :loading="loading">测试登录</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
    
    <div v-if="result" class="debug-result">
      <h4>测试结果</h4>
      <el-alert
        :title="result.success ? '登录成功' : '登录失败'"
        :type="result.success ? 'success' : 'error'"
        :description="result.message"
        show-icon
      />
      
      <div class="result-details">
        <h5>状态码: {{ result.status }}</h5>
        <div v-if="result.headers" class="headers">
          <h5>响应头:</h5>
          <pre>{{ JSON.stringify(result.headers, null, 2) }}</pre>
        </div>
        <div v-if="result.data" class="response-data">
          <h5>响应数据:</h5>
          <pre>{{ JSON.stringify(result.data, null, 2) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'

const loading = ref(false)
const result = ref(null)

const urlOptions = [
  { label: '/api/auth/login', value: '/api/auth/login' },
  { label: '/api/login', value: '/api/login' },
  { label: '/login', value: '/login' },
  { label: '/api/user/login', value: '/api/user/login' },
  { label: '/auth/login', value: '/auth/login' }
]

const form = reactive({
  url: '/api/auth/login',
  format: 'json',
  usernameField: 'username',
  passwordField: 'password',
  username: 'admin',
  password: '123456'
})

const testLogin = async () => {
  loading.value = true
  result.value = null
  
  try {
    // 构建请求数据
    let data
    if (form.format === 'json') {
      // JSON格式
      data = {}
      data[form.usernameField] = form.username
      data[form.passwordField] = form.password
    } else {
      // 表单数据
      data = new FormData()
      data.append(form.usernameField, form.username)
      data.append(form.passwordField, form.password)
    }
    
    // 设置请求配置
    const config = {
      headers: {
        'Content-Type': form.format === 'json' 
          ? 'application/json' 
          : 'application/x-www-form-urlencoded'
      }
    }
    
    // 发送请求
    const response = await axios.post(form.url, data, config)
    
    // 处理成功响应
    result.value = {
      success: true,
      status: response.status,
      headers: response.headers,
      data: response.data,
      message: '登录请求成功'
    }
    
    // 检查是否有token或成功标志
    if (response.data.token || 
        (response.data.data && response.data.data.token) ||
        response.data.success) {
      result.value.message = '登录成功，获取到认证信息'
    }
  } catch (error) {
    // 处理错误响应
    result.value = {
      success: false,
      status: error.response?.status,
      headers: error.response?.headers,
      data: error.response?.data,
      message: error.message
    }
    
    // 特殊处理401错误
    if (error.response?.status === 401) {
      result.value.message = '认证失败：路径正确，但凭据无效或格式错误'
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.url = '/api/auth/login'
  form.format = 'json'
  form.usernameField = 'username'
  form.passwordField = 'password'
  form.username = 'admin'
  form.password = '123456'
  result.value = null
}
</script>

<style scoped>
.login-form-debug {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.debug-result {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.result-details {
  margin-top: 15px;
}

.headers, .response-data {
  margin-top: 10px;
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

pre {
  white-space: pre-wrap;
  font-family: monospace;
  max-height: 300px;
  overflow: auto;
}
</style> 