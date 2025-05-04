import request from '@/utils/request'
import axios from 'axios' // 导入axios用于直接测试
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

// 获取档案列表
export function getArchiveList(params) {
  return enhancedRequest({
    url: '/archives',
    method: 'get',
    params
  })
}

// 获取档案详情
export function getArchiveDetail(id) {
  return enhancedRequest({
    url: `/archives/${id}`,
    method: 'get'
  })
}

// 创建新档案
export function createArchive(data) {
  return enhancedRequest({
    url: '/archives',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新档案
export function updateArchive(id, data) {
  return enhancedRequest({
    url: `/archives/${id}`,
    method: 'put',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除档案
export function deleteArchive(id) {
  return enhancedRequest({
    url: `/archives/${id}`,
    method: 'delete'
  })
}

// 批量删除档案
export function batchDeleteArchives(ids) {
  return enhancedRequest({
    url: '/archives/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

// 搜索档案
export function searchArchives(params) {
  return enhancedRequest({
    url: '/archives',
    method: 'get',
    params
  })
}

// 获取档案统计数据
export function getArchiveStatistics() {
  return enhancedRequest({
    url: '/archives/statistics',
    method: 'get'
  })
}

// 借阅档案
export function borrowArchive(id, data) {
  return enhancedRequest({
    url: `/archives/${id}/borrow`,
    method: 'post',
    data
  })
}

// 归还档案
export function returnArchive(id) {
  return enhancedRequest({
    url: `/archives/${id}/return`,
    method: 'put'
  })
}

// 批量导入档案
export function importArchives(data) {
  return enhancedRequest({
    url: '/archives/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出档案
export function exportArchives(params) {
  return enhancedRequest({
    url: '/archives/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 批量导出档案
export function batchExportArchives(ids) {
  return enhancedRequest({
    url: '/archives/batch-export',
    method: 'post',
    data: { ids },
    responseType: 'blob'
  })
}

// 档案预览
export function getArchivePreviewUrl(id) {
  return `/archives/${id}/preview`
}

// 档案下载
export function getArchiveDownloadUrl(id) {
  return `/archives/${id}/download`
}

// 下载单个档案
export function downloadArchive(id) {
  return enhancedRequest({
    url: `/archives/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取借阅记录
export function getBorrowRecords(params) {
  return enhancedRequest({
    url: '/archives/borrow/records',
    method: 'get',
    params
  })
}

// 获取档案分类
export function getArchiveCategories() {
  return enhancedRequest({
    url: '/archives/categories',
    method: 'get'
  })
}

// 获取档案标签
export function getArchiveTags() {
  return enhancedRequest({
    url: '/archives/tags',
    method: 'get'
  })
}

// 添加档案标签
export function addArchiveTag(data) {
  return enhancedRequest({
    url: '/archives/tags',
    method: 'post',
    data
  })
}

// 获取档案审批流程
export function getArchiveApprovalProcess(id) {
  return enhancedRequest({
    url: `/archives/${id}/approval-process`,
    method: 'get'
  })
}

// 提交档案审批
export function submitArchiveApproval(id, data) {
  return enhancedRequest({
    url: `/archives/${id}/submit-approval`,
    method: 'post',
    data
  })
}

// 审批档案
export function approveArchive(id, data) {
  return enhancedRequest({
    url: `/archives/${id}/approve`,
    method: 'post',
    data
  })
}

// 调试用接口 - 检查API连接状态
export function checkApiConnection() {
  return enhancedRequest({
    url: '/health-check',
    method: 'get',
    timeout: 5000
  })
}

// 调试用接口 - 获取当前环境配置
export function getEnvironmentConfig() {
  return enhancedRequest({
    url: '/environment-config',
    method: 'get'
  })
}

// 测试档案列表请求
export function testArchiveListRequest(params) {
  return enhancedRequest({
    url: '/archives',
    method: 'get',
    params,
    hideErrorMessage: true
  })
} 