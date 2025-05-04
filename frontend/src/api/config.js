/**
 * API配置文件
 * 用于集中管理API相关配置
 */

import { getEnvValue, getMode, isDevelopment } from '@/utils/env';

// 默认API配置
export const apiConfig = {
  // 基础URL，优先使用环境变量中的配置
  baseURL: getEnvValue('VITE_BASE_API', '/api'),
  
  // 请求超时时间(毫秒)
  timeout: parseInt(getEnvValue('VITE_API_TIMEOUT', '30000')),
  
  // 调试模式
  debug: getEnvValue('VITE_ENABLE_DEBUG', 'false') === 'true',
  
  // 后端服务地址（用于直接测试）
  backendUrl: getEnvValue('VITE_BACKEND_URL', 'http://localhost:8080'),
  
  // 路径前缀转换规则
  pathRewrite: {
    // 不需要重写路径，注释掉
    // '^/api': '/archive'
  }
}

// API健康检查配置
export const healthCheckEndpoints = {
  // 代理健康检查
  proxy: '/api/health',
  
  // 后端直连健康检查
  backend: '/archive/health',
  
  // 测试端点
  test: '/archives?limit=1'
}

// API错误码映射
export const errorCodeMap = {
  400: '请求参数错误',
  401: '未授权，请重新登录',
  403: '拒绝访问',
  404: '请求的资源不存在',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务不可用',
  504: '网关超时'
}

// 根据环境获取实际API配置
export function getEnvironmentConfig() {
  const env = getMode();
  console.log(`当前环境: ${env}`);
  
  return {
    ...apiConfig,
    environment: env,
    isDevelopment: isDevelopment(),
    isProduction: env === 'production',
    mockEnabled: isDevelopment() && getEnvValue('VITE_ENABLE_MOCK', 'false') === 'true'
  }
}

export default {
  apiConfig,
  healthCheckEndpoints,
  errorCodeMap,
  getEnvironmentConfig
} 