import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { apiConfig, errorCodeMap } from '@/api/config'

/**
 * 创建axios实例
 * 使用从config.js导入的集中配置
 */
const service = axios.create({
  baseURL: apiConfig.baseURL,
  timeout: apiConfig.timeout
})

// 调试模式 - 在控制台显示详细日志
const DEBUG = apiConfig.debug

// 请求拦截器增强版
service.interceptors.request.use(
  config => {
    // 完整的请求URL (包括baseURL)
    const fullUrl = `${config.baseURL || ''}${config.url}`
    
    if (DEBUG) {
      console.group(`请求: ${config.method.toUpperCase()} ${fullUrl}`)
      console.log('请求头:', config.headers)
      console.log('请求参数:', config.params)
      console.log('请求体:', config.data)
      console.groupEnd()
    }

    // 检查token是否存在，增加更严格的错误处理
    try {
      // 只有当请求需要认证时才添加token
      if (config.requiresAuth !== false) {
        try {
          const userStore = useUserStore()
          if (userStore && userStore.token) {
            config.headers['Authorization'] = `Bearer ${userStore.token}`
          } else {
            console.warn('用户Token不存在，请求可能会被拒绝')
          }
        } catch (e) {
          console.error('获取token时出错:', e)
          // 即使token获取失败，也继续请求，让后端决定是否接受此请求
        }
      }
    } catch (e) {
      console.error('请求拦截器错误:', e)
      // 即使拦截器出错，也继续请求
    }
    
    // 添加请求标识符，用于重试或取消
    config.requestId = Date.now().toString(36) + Math.random().toString(36).slice(2)
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器增强版
service.interceptors.response.use(
  response => {
    const res = response.data
    const fullUrl = `${response.config.baseURL || ''}${response.config.url}`
    
    if (DEBUG) {
      console.group(`响应: ${response.config.method.toUpperCase()} ${fullUrl}`)
      console.log('状态码:', response.status)
      console.log('响应头:', response.headers)
      console.log('响应数据:', res)
      console.groupEnd()
    }

    // 对于文件下载等二进制响应，直接返回响应对象
    const contentType = response.headers['content-type']
    if (response.config.responseType === 'blob' || (contentType && contentType.includes('application/octet-stream'))) {
      return {
        code: 200,
        data: response.data,
        headers: response.headers
      }
    }

    // 检查响应状态码
    if (response.status >= 200 && response.status < 300) {
      // 如果后端返回的不是标准结构，进行适配
      if (typeof res === 'object' && res !== null && 'code' in res) {
        // 标准响应格式，直接返回
        return res
      } else {
        // 非标准响应格式，进行适配
        return {
          code: 200,
          data: res,
          message: '请求成功'
        }
      }
    } else {
      // HTTP状态码不是2xx
      const errorMsg = response.statusText || errorCodeMap[response.status] || '请求失败'
      ElMessage.error(errorMsg)
      console.error('HTTP错误:', response.status, errorMsg)
      return {
        code: response.status,
        message: errorMsg,
        data: null
      }
    }
  },
  error => {
    console.group('响应错误')
    console.error('错误信息:', error.message)
    
    let errorMessage = '请求失败'
    let errorCode = 500
    
    if (error.response) {
      // 服务器返回了错误响应
      errorCode = error.response.status
      console.error('状态码:', errorCode)
      console.error('响应数据:', error.response.data)
      console.error('响应头:', error.response.headers)
      console.error('请求配置:', error.config)
      
      // 尝试从响应中获取详细错误信息
      if (error.response.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error
        }
      } else {
        errorMessage = error.response.statusText || errorCodeMap[errorCode] || `HTTP错误 ${errorCode}`
      }
      
      // 处理特定的HTTP状态码
      if (errorCode === 401) {
        errorMessage = '登录已过期，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')

        if (typeof window !== 'undefined') {
          setTimeout(() => {
            window.location.href = '/login'
          }, 1500)
        }
      } else if (errorCode === 403) {
        errorMessage = '无权限访问此资源'
      } else if (errorCode === 404) {
        errorMessage = '请求的资源不存在'
      } else if (errorCode === 500) {
        errorMessage = '服务器内部错误'
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('无响应:', error.request)
      errorMessage = '服务器未响应，请检查网络连接或联系管理员'
    } else {
      // 设置请求时发生错误
      console.error('请求错误:', error)
      errorMessage = error.message || '请求过程中发生错误'
    }
    
    console.groupEnd()
    
    // 避免在测试连接时显示错误提示
    if (!error.config?.hideErrorMessage) {
      ElMessage.error(errorMessage)
    }
    
    // 返回标准错误对象，方便上层统一处理
    return {
      code: errorCode,
      message: errorMessage,
      error: true
    }
  }
)

/**
 * 发送请求并自动重试
 * @param {Object} config - 请求配置
 * @param {Number} retries - 最大重试次数
 * @param {Number} retryDelay - 重试延迟(毫秒)
 */
export function requestWithRetry(config, retries = 3, retryDelay = 1000) {
  return new Promise((resolve, reject) => {
    const attempt = async (retryCount) => {
      try {
        const response = await service(config)
        resolve(response)
      } catch (err) {
        if (retryCount < retries && shouldRetry(err)) {
          console.log(`请求失败，${retryDelay/1000}秒后重试(${retryCount + 1}/${retries})...`)
          setTimeout(() => attempt(retryCount + 1), retryDelay)
        } else {
          reject(err)
        }
      }
    }
    
    attempt(0)
  })
}

// 判断是否应该重试请求
function shouldRetry(error) {
  // 网络错误或服务器5xx错误才重试
  return !error.response || (error.response && error.response.status >= 500)
}

export default service 