import axios from 'axios'
import request from './request'
import { apiConfig, healthCheckEndpoints } from '@/api/config'

/**
 * API连接检查工具
 * 用于测试后端API连接是否正常
 */

// 测试VITE代理连接
export async function checkViteProxy() {
  try {
    const response = await axios.get(healthCheckEndpoints.proxy, { 
      timeout: 5000,
      headers: {
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache',
        'Expires': '0'
      }
    })
    return {
      success: response.status === 200,
      message: response.status === 200 ? '代理服务器连接成功' : '代理服务器响应异常',
      data: response.data,
      status: response.status
    }
  } catch (error) {
    console.error('代理服务器检查失败:', error)
    return {
      success: false,
      message: error.message || '代理服务器连接失败',
      error: error,
      details: {
        hasResponse: !!error.response,
        status: error.response?.status,
        data: error.response?.data
      }
    }
  }
}

// 测试后端直接连接
export async function checkDirectBackend(baseUrl = apiConfig.backendUrl) {
  try {
    const response = await axios.get(`${baseUrl}${healthCheckEndpoints.backend}`, { 
      timeout: 5000,
      headers: {
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache',
        'Expires': '0'
      }
    })
    return {
      success: response.status === 200,
      message: response.status === 200 ? '后端服务器连接成功' : '后端服务器响应异常',
      data: response.data,
      status: response.status
    }
  } catch (error) {
    console.error('后端服务器检查失败:', error)
    return {
      success: false,
      message: error.message || '后端服务器连接失败',
      error: error,
      details: {
        hasResponse: !!error.response,
        status: error.response?.status,
        data: error.response?.data
      }
    }
  }
}

// 测试完整API请求，使用request实例
export async function testApiWithRequest(url = healthCheckEndpoints.test) {
  try {
    const response = await request({
      url,
      method: 'get',
      timeout: 5000,
      hideErrorMessage: true, // 防止在UI上显示错误
      requiresAuth: false // 不需要身份验证
    })
    return {
      success: response.code === 200,
      message: response.code === 200 ? 'API请求成功' : `API请求失败: ${response.message}`,
      data: response.data,
      response
    }
  } catch (error) {
    console.error('API请求测试失败:', error)
    return {
      success: false,
      message: error.message || 'API请求测试失败',
      error: error
    }
  }
}

// 获取网络诊断信息
export async function getNetworkDiagnostics() {
  const viteProxy = await checkViteProxy()
  const directBackend = await checkDirectBackend()
  const apiTest = await testApiWithRequest()
  
  return {
    timestamp: new Date().toISOString(),
    viteProxy,
    directBackend,
    apiTest,
    environment: {
      nodeEnv: import.meta.env.MODE,
      baseApi: apiConfig.baseURL,
      browser: navigator.userAgent,
      location: window.location.href
    }
  }
}

// 测试API端点的可用性
export async function testEndpoint(url, method = 'get', data = null) {
  try {
    const config = {
      url,
      method,
      timeout: 5000,
      hideErrorMessage: true
    }
    
    if (data && (method === 'post' || method === 'put' || method === 'patch')) {
      config.data = data
    } else if (data) {
      config.params = data
    }
    
    const response = await request(config)
    
    return {
      success: response.code === 200,
      message: response.code === 200 ? '端点测试成功' : `端点测试失败: ${response.message}`,
      data: response.data,
      response
    }
  } catch (error) {
    return {
      success: false,
      message: error.message || '端点测试失败',
      error: error
    }
  }
}

export default {
  checkViteProxy,
  checkDirectBackend,
  testApiWithRequest,
  getNetworkDiagnostics,
  testEndpoint
} 