/**
 * API模块入口
 * 统一导出所有API
 */

import * as userApi from './user'
import * as archiveApi from './archive'
import * as chatApi from './chat'
import * as templateApi from './template'
import { apiConfig, getEnvironmentConfig } from './config'
import { checkViteProxy, checkDirectBackend, testApiWithRequest } from '@/utils/api-check'

// 导出所有API模块
export default {
  user: userApi,
  archive: archiveApi,
  chat: chatApi,
  template: templateApi,
  
  // 获取API配置
  getConfig() {
    return apiConfig
  },
  
  // 获取环境配置
  getEnvironmentConfig,
  
  // API测试方法
  async testConnection() {
    console.log('开始API连接测试...')
    
    // 测试Vite代理
    const proxyTest = await checkViteProxy()
    
    // 测试后端直连
    const backendTest = await checkDirectBackend(apiConfig.backendUrl)
    
    // 测试档案列表API
    const archiveTest = await testApiWithRequest('/archives')
    
    return {
      timestamp: new Date().toISOString(),
      proxy: proxyTest,
      backend: backendTest,
      archive: archiveTest,
      config: getEnvironmentConfig()
    }
  }
} 