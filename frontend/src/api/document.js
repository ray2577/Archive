import request from '@/utils/request'
import { getEnvValue } from '@/utils/env'

/**
 * 增强型API调用工具函数
 * @param {Object} config - 请求配置
 * @returns {Promise} - API响应
 */
const enhancedRequest = (config) => {
  const defaultConfig = {
    timeout: 30000,
    validateStatus: status => status >= 200 && status < 500,
    showErrorMessage: true
  }
  
  // 合并默认配置和自定义配置
  const finalConfig = { ...defaultConfig, ...config }
  
  // 添加请求开始的日志
  console.log(`[API][${finalConfig.method?.toUpperCase()}] ${finalConfig.url} - 开始请求`)
  
  return request(finalConfig).catch(error => {
    console.error(`[API][${finalConfig.method?.toUpperCase()}] ${finalConfig.url} - 请求失败:`, error)
    return {
      code: error.response?.status || 500,
      message: error.message || '网络请求失败，请检查网络连接或联系管理员',
      error: error
    }
  })
}

// 获取文档列表
export function getDocumentList(params) {
  return enhancedRequest({
    url: '/documents',
    method: 'get',
    params
  })
}

// 获取文档详情
export function getDocumentDetail(id) {
  return enhancedRequest({
    url: `/documents/${id}`,
    method: 'get'
  })
}

// 创建新文档
export function createDocument(data) {
  return enhancedRequest({
    url: '/documents',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新文档
export function updateDocument(id, data) {
  return enhancedRequest({
    url: `/documents/${id}`,
    method: 'put',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文档
export function deleteDocument(id) {
  return enhancedRequest({
    url: `/documents/${id}`,
    method: 'delete'
  })
}

// 批量删除文档
export function batchDeleteDocuments(ids) {
  return enhancedRequest({
    url: '/documents/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

// 搜索文档
export function searchDocuments(params) {
  return enhancedRequest({
    url: '/documents',
    method: 'get',
    params
  })
}

// 获取文档统计数据
export function getDocumentStatistics() {
  return enhancedRequest({
    url: '/documents/statistics',
    method: 'get'
  })
}

// 分享文档
export function shareDocument(id, data) {
  return enhancedRequest({
    url: `/documents/${id}/share`,
    method: 'post',
    data
  })
}

// 批量导入文档
export function importDocuments(data) {
  return enhancedRequest({
    url: '/documents/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出文档
export function exportDocuments(params) {
  return enhancedRequest({
    url: '/documents/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 批量导出文档
export function batchExportDocuments(ids) {
  return enhancedRequest({
    url: '/documents/batch-export',
    method: 'post',
    data: { ids },
    responseType: 'blob'
  })
}

// 文档预览
export function getDocumentPreviewUrl(id) {
  return `/documents/${id}/preview`
}

// 文档下载
export function downloadDocument(id) {
  return enhancedRequest({
    url: `/documents/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取文档分类
export function getDocumentCategories() {
  return enhancedRequest({
    url: '/documents/categories',
    method: 'get'
  })
}

// 获取文档标签
export function getDocumentTags() {
  return enhancedRequest({
    url: '/documents/tags',
    method: 'get'
  })
}

// 添加文档标签
export function addDocumentTag(data) {
  return enhancedRequest({
    url: '/documents/tags',
    method: 'post',
    data
  })
}

// 测试文档API连接
export function testDocumentListRequest(params) {
  return enhancedRequest({
    url: '/documents',
    method: 'get',
    params,
    timeout: 5000,
    validateStatus: status => true // 允许任何状态码以便查看错误详情
  })
} 