import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 创建axios实例
 * 当请求不需要经过Vite代理时（例如直接请求后端），可以设置VITE_API_BASE_URL环境变量
 * 否则，baseURL设为空字符串，确保请求路径不会有多余的前缀
 */
const service = axios.create({
  baseURL: import.meta.env.VITE_BASE_API,
  timeout: 30000
})

// 请求拦截器增强版
service.interceptors.request.use(
  config => {
    // 完整的请求URL (包括baseURL)
    const fullUrl = `${config.baseURL || ''}${config.url}`
    console.group(`请求: ${config.method.toUpperCase()} ${fullUrl}`)
    console.log('请求头:', config.headers)
    console.log('请求参数:', config.params)
    console.log('请求体:', config.data)
    console.groupEnd()

    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器增强版
service.interceptors.response.use(
  response => {
    const res = response.data
    const fullUrl = `${response.config.baseURL || ''}${response.config.url}`
    
    console.group(`响应: ${response.config.method.toUpperCase()} ${fullUrl}`)
    console.log('状态码:', response.status)
    console.log('响应头:', response.headers)
    console.log('响应数据:', res)
    console.groupEnd()

    if (response.status !== 200) {
      ElMessage.error(response.statusText || '请求失败')
      return Promise.reject(new Error(res.statusText || '请求失败'))
    }

    return res
  },
  error => {
    console.group('响应错误')
    console.error('错误信息:', error.message)
    
    if (error.response) {
      console.error('状态码:', error.response.status)
      console.error('响应数据:', error.response.data)
      console.error('响应头:', error.response.headers)
      console.error('请求配置:', error.config)
    } else {
      console.error('无响应对象:', error)
    }
    console.groupEnd()
    
    // 处理401等错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');

      ElMessage.error('登录已过期，请重新登录');

      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
      return;
    }
    
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service 