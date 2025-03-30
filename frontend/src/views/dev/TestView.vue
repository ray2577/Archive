<template>
  <div class="test-container">
    <h1>后端连接测试</h1>
    <div class="test-buttons">
      <el-button type="primary" @click="testConnection">测试连接</el-button>
      <el-button type="success" @click="testDirectConnection">测试直接连接</el-button>
      <el-button type="primary" @click="testSimpleEndpoint">测试简单端点</el-button>
      <el-button type="warning" @click="testEndpointNoPrefix">测试无前缀端点</el-button>
      <el-button type="danger" @click="debugLoginRequest">调试登录请求</el-button>
      <el-button type="danger" @click="debugDirectLoginTest">直接登录测试</el-button>
      <el-button type="warning" @click="debugJwtIssues">调试JWT问题</el-button>
      <el-button type="success" @click="testJwtGeneration">测试JWT生成</el-button>
    </div>

    <div class="divider"></div>
    
    <h2>账户测试</h2>
    <div class="test-buttons">
      <el-button type="warning" @click="checkAdmin">检查管理员账户</el-button>
      <el-button type="danger" @click="testLogin">测试登录</el-button>
      <el-button type="info" @click="resetAdmin">重置管理员密码</el-button>
      <el-button type="primary" @click="testDatabase">测试数据库</el-button>
      <el-button type="success" @click="testSimpleLogin">简单登录测试</el-button>
    </div>
    
    <div class="login-form" v-if="showLoginForm">
      <el-form>
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" @click="submitLogin">登录</el-button>
      </el-form>
    </div>
    
    <div v-if="result" class="result">
      <h3>测试结果:</h3>
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
    <div v-if="error" class="error">
      <h3>错误信息:</h3>
      <pre>{{ error }}</pre>
    </div>

    <div class="test-buttons">
      <el-button type="primary" @click="testUrl('/test')">测试 /test</el-button>
      <el-button type="primary" @click="testUrl('/api/test')">测试 /api/test</el-button>
      <el-button type="primary" @click="testUrl('/archive/test')">测试 /archive/test</el-button>
      <el-button type="primary" @click="testUrl('/archive/api/test')">测试 /archive/api/test</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const result = ref(null)
const error = ref(null)
const showLoginForm = ref(false)
const loginForm = ref({
  username: 'admin',
  password: 'admin123'
})

// 测试通过代理连接
const testConnection = async () => {
  result.value = null
  error.value = null
  try {
    // 使用配置了代理的请求对象，使用 /api 前缀
    const response = await request.get('/api/auth/health')
    result.value = response
  } catch (err) {
    error.value = `${err.message}\n${JSON.stringify(err, null, 2)}`
    console.error('测试连接失败', err)
  }
}

// 测试直接连接
const testDirectConnection = async () => {
  result.value = null
  error.value = null
  try {
    // 直接使用axios连接后端
    const response = await axios.get('http://localhost:8080/archive/auth/health')
    result.value = response.data
  } catch (err) {
    error.value = `${err.message}\n${JSON.stringify(err, null, 2)}`
    console.error('直接连接失败', err)
  }
}

// 检查管理员账户
const checkAdmin = async () => {
  result.value = null
  error.value = null
  try {
    const response = await request.get('/api/auth/check-admin')
    result.value = response
  } catch (err) {
    error.value = `${err.message}\n${JSON.stringify(err, null, 2)}`
    console.error('检查管理员账户失败', err)
  }
}

// 测试登录表单
const testLogin = () => {
  showLoginForm.value = true
}

// 提交登录
const submitLogin = async () => {
  result.value = null
  error.value = null
  try {
    console.log('提交登录请求 - 使用fetch API:', loginForm.value);
    
    // 准备请求数据
    const loginData = {
      username: loginForm.value.username,
      password: loginForm.value.password
    };
    
    console.log('请求数据:', JSON.stringify(loginData, null, 2));
    
    // 使用fetch API发送请求
    const response = await fetch('http://localhost:8080/archive/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(loginData)
    });
    
    console.log('响应状态:', response.status);
    console.log('响应头:', response.headers);
    
    // 解析响应
    const contentType = response.headers.get('content-type');
    let responseData;
    
    if (contentType && contentType.includes('application/json')) {
      responseData = await response.json();
      console.log('JSON响应数据:', responseData);
    } else {
      responseData = await response.text();
      console.log('文本响应数据:', responseData);
    }
    
    result.value = responseData;
    
    // 如果成功，显示成功消息
    if (response.ok) {
      ElMessage.success('登录成功');
    }
  } catch (err) {
    console.error('登录失败详情:', err);
    error.value = `登录请求失败: ${err.message}`;
  }
}

// 重置管理员密码
const resetAdmin = async () => {
  result.value = null
  error.value = null
  try {
    console.log('重置管理员密码...');
    const response = await fetch('http://localhost:8080/archive/auth/reset-admin', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    
    if (response.ok) {
      const responseData = await response.json();
      result.value = responseData;
      
      // 更新密码为新密码
      if (responseData.newPassword) {
        loginForm.value.password = responseData.newPassword;
        console.log('更新密码为:', responseData.newPassword);
      }
      
      ElMessage.success('管理员密码已重置，请使用新密码登录');
    } else {
      const errorText = await response.text();
      error.value = `重置密码失败: ${response.status} - ${errorText}`;
    }
  } catch (err) {
    console.error('重置管理员密码失败:', err);
    error.value = `重置管理员密码失败: ${err.message}`;
  }
}

// 测试数据库连接
const testDatabase = async () => {
  result.value = null
  error.value = null
  try {
    const response = await request.get('/api/auth/test-db')
    result.value = response
    ElMessage.success('数据库连接正常')
  } catch (err) {
    let errorDetail = '';
    if (err.response) {
      errorDetail = `状态码: ${err.response.status}\n`;
      if (err.response.data) {
        errorDetail += `错误内容: ${typeof err.response.data === 'string' ? err.response.data : JSON.stringify(err.response.data, null, 2)}\n`;
      }
    }
    error.value = `错误消息: ${err.message}\n${errorDetail}`;
    console.error('数据库连接测试失败', err)
  }
}

// 简单登录测试
const testSimpleLogin = async () => {
  result.value = null
  error.value = null
  try {
    // 使用临时axios实例确保一致
    const simpleLoginAxios = axios.create({
      baseURL: '',
      timeout: 30000
    });
    
    console.log('尝试简单登录测试 (login-test)');
    const response = await simpleLoginAxios.post('/api/auth/login-test', 
      {
        username: 'admin',
        password: 'admin123'
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      }
    );
    
    result.value = response.data;
    console.log('简单登录测试响应:', response.data);
    ElMessage.success('简单登录测试成功')
  } catch (err) {
    console.error('简单登录测试失败:', err);
    let errorDetail = '';
    if (err.response) {
      errorDetail = `状态码: ${err.response.status}\n`;
      if (err.response.data) {
        errorDetail += `错误内容: ${typeof err.response.data === 'string' ? err.response.data : JSON.stringify(err.response.data, null, 2)}\n`;
      }
      errorDetail += `Headers: ${JSON.stringify(err.response.headers, null, 2)}\n`;
    }
    error.value = `简单登录测试失败: ${err.message}\n${errorDetail}`;
  }
}

// 测试不同的 URL 路径
const testUrl = async (url) => {
  result.value = null
  error.value = null
  try {
    // 直接使用完整 URL
    const response = await axios.get(`http://localhost:8080${url}`)
    result.value = {
      url,
      response: response.data
    }
  } catch (err) {
    let errorDetail = '';
    if (err.response) {
      errorDetail = `状态码: ${err.response.status}\n`;
      if (err.response.data) {
        errorDetail += `错误内容: ${typeof err.response.data === 'string' ? err.response.data : JSON.stringify(err.response.data, null, 2)}\n`;
      }
    }
    error.value = `测试 ${url} 失败\n${err.message}\n${errorDetail}`;
    console.error(`测试 ${url} 失败`, err)
  }
}

// 测试简单端点
const testSimpleEndpoint = async () => {
  result.value = null
  error.value = null
  try {
    const response = await request.get('/api/auth/simple-test')
    result.value = response
  } catch (err) {
    error.value = `${err.message}\n${JSON.stringify(err, null, 2)}`
    console.error('测试简单端点失败', err)
  }
}

// 测试没有 /api 前缀的简单端点
const testEndpointNoPrefix = async () => {
  result.value = null
  error.value = null
  try {
    // 使用没有 /api 前缀的路径
    const response = await request.get('/auth/simple-test')
    result.value = response
  } catch (err) {
    error.value = `${err.message}\n${JSON.stringify(err, null, 2)}`
    console.error('测试无前缀端点失败', err)
  }
}

// 调试登录请求 - 测试请求URL
const debugLoginRequest = async () => {
  result.value = null
  error.value = null
  
  // 显式创建新的axios实例进行测试，不使用request.js中的实例
  const testAxios = axios.create({
    baseURL: '', // 确保没有baseURL
    timeout: 30000
  });
  
  try {
    console.log('尝试登录 - 版本1 (标准JSON + 内容类型头)');
    const response = await testAxios.post('http://localhost:8080/archive/auth/login', 
      {
        username: 'admin',
        password: 'admin123'
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      }
    );
    result.value = {
      message: '直接请求成功',
      data: response.data
    };
  } catch (err) {
    console.error('登录请求失败 - 版本1:', err);
    console.log('尝试登录 - 版本2 (变更请求格式)');
    
    try {
      // 尝试另一种请求格式，使用 x-www-form-urlencoded
      const formData = new URLSearchParams();
      formData.append('username', 'admin');
      formData.append('password', 'admin123');
      
      const response = await testAxios.post('http://localhost:8080/archive/auth/login', 
        formData,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json'
          }
        }
      );
      
      result.value = {
        message: '直接请求成功 (版本2)',
        data: response.data
      };
    } catch (secondErr) {
      console.error('登录请求失败 - 版本2:', secondErr);
      
      // 显示两次尝试的错误信息
      let errorDetail = '';
      
      if (err.response) {
        errorDetail += `版本1错误状态: ${err.response.status}\n`;
        errorDetail += `版本1错误内容: ${JSON.stringify(err.response.data || {}, null, 2)}\n\n`;
      }
      
      if (secondErr.response) {
        errorDetail += `版本2错误状态: ${secondErr.response.status}\n`;
        errorDetail += `版本2错误内容: ${JSON.stringify(secondErr.response.data || {}, null, 2)}\n`;
      }
      
      error.value = `直接请求失败:\n${errorDetail}`;
    }
  }
}

// 直接登录测试 - 使用fetch API
const debugDirectLoginTest = async () => {
  result.value = null
  error.value = null
  
  try {
    console.log('使用fetch API尝试直接登录');
    
    // 准备请求数据
    const loginData = {
      username: 'admin',
      password: 'admin123'
    };
    
    console.log('请求数据:', JSON.stringify(loginData, null, 2));
    
    // 使用fetch API发送请求
    const response = await fetch('http://localhost:8080/archive/auth/login-test', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(loginData)
    });
    
    console.log('响应状态:', response.status);
    console.log('响应头:', response.headers);
    
    // 解析响应
    const contentType = response.headers.get('content-type');
    let responseData;
    
    if (contentType && contentType.includes('application/json')) {
      responseData = await response.json();
      console.log('JSON响应数据:', responseData);
    } else {
      responseData = await response.text();
      console.log('文本响应数据:', responseData);
    }
    
    result.value = {
      status: response.status,
      statusText: response.statusText,
      data: responseData
    };
  } catch (err) {
    console.error('Fetch请求失败:', err);
    error.value = `Fetch请求失败: ${err.message}`;
  }
}

// 调试JWT相关问题
const debugJwtIssues = async () => {
  result.value = null
  error.value = null
  
  // 检查可能的问题
  const checkResults = [];
  
  try {
    console.log('正在检查数据库连接...');
    // 1. 检查数据库连接
    const dbResponse = await fetch('http://localhost:8080/archive/auth/test-db', {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    if (dbResponse.ok) {
      const dbData = await dbResponse.json();
      checkResults.push({
        test: '数据库连接',
        status: 'OK',
        details: dbData
      });
    } else {
      const errorText = await dbResponse.text();
      checkResults.push({
        test: '数据库连接',
        status: 'ERROR',
        message: `状态码: ${dbResponse.status}`,
        details: errorText
      });
    }
  } catch (dbErr) {
    checkResults.push({
      test: '数据库连接',
      status: 'ERROR',
      message: dbErr.message
    });
  }
  
  try {
    console.log('正在检查管理员账户...');
    // 2. 检查管理员账户
    const adminResponse = await fetch('http://localhost:8080/archive/auth/check-admin', {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    if (adminResponse.ok) {
      const adminData = await adminResponse.json();
      checkResults.push({
        test: '管理员账户',
        status: 'OK',
        details: adminData
      });
    } else {
      const errorText = await adminResponse.text();
      checkResults.push({
        test: '管理员账户',
        status: 'ERROR',
        message: `状态码: ${adminResponse.status}`,
        details: errorText
      });
    }
  } catch (adminErr) {
    checkResults.push({
      test: '管理员账户',
      status: 'ERROR',
      message: adminErr.message
    });
  }
  
  try {
    console.log('正在尝试简单登录...');
    // 3. 尝试简单登录测试
    const testLoginResponse = await fetch('http://localhost:8080/archive/auth/login-test', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({
        username: 'admin',
        password: 'admin123'
      })
    });
    
    if (testLoginResponse.ok) {
      const testLoginData = await testLoginResponse.json();
      checkResults.push({
        test: '简单登录测试',
        status: 'OK',
        details: testLoginData
      });
    } else {
      const errorText = await testLoginResponse.text();
      checkResults.push({
        test: '简单登录测试',
        status: 'ERROR',
        message: `状态码: ${testLoginResponse.status}`,
        details: errorText
      });
    }
  } catch (testLoginErr) {
    checkResults.push({
      test: '简单登录测试',
      status: 'ERROR',
      message: testLoginErr.message
    });
  }
  
  // 显示结果
  result.value = {
    message: 'JWT诊断结果',
    results: checkResults,
    recommendations: []
  };
  
  // 分析并提供建议
  const allSuccessful = checkResults.every(r => r.status === 'OK');
  const dbOk = checkResults.find(r => r.test === '数据库连接')?.status === 'OK';
  const adminOk = checkResults.find(r => r.test === '管理员账户')?.status === 'OK';
  const testLoginOk = checkResults.find(r => r.test === '简单登录测试')?.status === 'OK';
  
  if (allSuccessful) {
    result.value.recommendations.push('所有测试通过，JWT问题可能与密钥或配置有关。尝试重置管理员密码。');
  } else {
    if (!dbOk) {
      result.value.recommendations.push('数据库连接测试失败。请检查数据库配置和连接。');
    }
    if (!adminOk) {
      result.value.recommendations.push('管理员账户测试失败。可能需要重置管理员密码。');
    }
    if (!testLoginOk) {
      result.value.recommendations.push('简单登录测试失败。检查认证逻辑和用户凭据。');
    }
    if (dbOk && adminOk && testLoginOk) {
      result.value.recommendations.push('基础测试通过但JWT仍有问题。检查JWT密钥配置和令牌生成逻辑。');
    }
  }
}

// 测试JWT生成
const testJwtGeneration = async () => {
  result.value = null
  error.value = null
  
  try {
    console.log('测试JWT令牌生成...');
    
    const response = await fetch('http://localhost:8080/archive/auth/test-jwt', {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    console.log('响应状态:', response.status);
    
    if (response.ok) {
      const data = await response.json();
      console.log('JWT测试响应:', data);
      result.value = data;
      
      if (data.success) {
        ElMessage.success('JWT令牌生成测试成功');
      } else {
        ElMessage.error('JWT令牌生成测试失败');
      }
    } else {
      const errorText = await response.text();
      error.value = `JWT测试失败: ${response.status} - ${errorText}`;
    }
  } catch (err) {
    console.error('JWT测试请求失败:', err);
    error.value = `JWT测试请求失败: ${err.message}`;
  }
}
</script>

<style scoped>
.test-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.test-buttons {
  margin: 20px 0;
}

.result, .error {
  margin-top: 20px;
  padding: 15px;
  border-radius: 5px;
}

.result {
  background-color: #f0f9eb;
}

.error {
  background-color: #fef0f0;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}

.divider {
  margin: 20px 0;
  border-top: 1px solid #eee;
}

.login-form {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
}
</style> 