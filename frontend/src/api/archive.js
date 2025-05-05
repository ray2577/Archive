import request from '@/utils/request'
import axios from 'axios' // 导入axios用于直接测试
import { getEnvValue } from '@/utils/env'

// 获取档案列表
export function getArchiveList(params) {
  console.log('调用档案列表API，参数:', params)
  return request({
    url: '/archives',
    method: 'get',
    params,
    timeout: 30000, // 增加超时时间
    // 添加请求错误处理
    validateStatus: function (status) {
      return status >= 200 && status < 500 // 接受状态码在200-499之间的响应，以便于在错误时获取更多信息
    }
  }).catch(error => {
    console.error('获取档案列表接口错误:', error)
    // 返回一个格式化的错误对象，便于统一处理
    return {
      code: 500,
      message: error.message || '网络请求失败，请检查网络连接或联系管理员',
      error: error
    }
  })
}

// 获取档案详情
export function getArchiveDetail(id) {
  return request({
    url: `/archives/${id}`,
    method: 'get'
  })
}

// 创建新档案
export function createArchive(data) {
  return request({
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
  return request({
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
  return request({
    url: `/archives/${id}`,
    method: 'delete'
  })
}

// 批量删除档案
export function batchDeleteArchives(ids) {
  return request({
    url: '/archives/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

// 搜索档案
export function searchArchives(params) {
  console.log('调用搜索API，参数:', params)
  // 临时使用基本的档案列表接口代替专门的搜索接口
  return request({
    url: '/archives',
    method: 'get',
    params,
    timeout: 30000
  }).catch(error => {
    console.error('搜索接口错误:', error)
    return {
      code: 500,
      message: error.message || '搜索请求失败',
      error: error
    }
  })
}

// 获取档案统计数据
export function getArchiveStatistics() {
  return request({
    url: '/archives/statistics',
    method: 'get'
  })
}

// 借阅档案
export function borrowArchive(id, data) {
  return request({
    url: `/archives/${id}/borrow`,
    method: 'post',
    data
  })
}

// 归还档案
export function returnArchive(id) {
  return request({
    url: `/archives/${id}/return`,
    method: 'put'
  })
}

// 批量导入档案
export function importArchives(data) {
  return request({
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
  return request({
    url: '/archives/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 批量导出档案
export function batchExportArchives(ids) {
  return request({
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
  return request({
    url: `/archives/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取借阅记录
export function getBorrowRecords(params) {
  return request({
    url: '/archives/borrow/records',
    method: 'get',
    params
  })
}

// 获取档案分类
export function getArchiveCategories() {
  return request({
    url: '/archives/categories',
    method: 'get'
  })
}

// 获取档案标签
export function getArchiveTags() {
  return request({
    url: '/archives/tags',
    method: 'get'
  })
}

// 添加档案标签
export function addArchiveTag(data) {
  return request({
    url: '/archives/tags',
    method: 'post',
    data
  })
}

// 获取档案审批流程
export function getArchiveApprovalProcess(id) {
  return request({
    url: `/archives/${id}/approval-process`,
    method: 'get'
  })
}

// 提交档案审批
export function submitArchiveApproval(id, data) {
  return request({
    url: `/archives/${id}/submit-approval`,
    method: 'post',
    data
  })
}

// 审批档案
export function approveArchive(id, data) {
  return request({
    url: `/archives/${id}/approve`,
    method: 'post',
    data
  })
}

// 调试用接口 - 检查API连接状态
export function checkApiConnection() {
  console.log('检查API连接状态')
  return request({
    url: '/health-check',
    method: 'get',
    timeout: 5000
  }).catch(error => {
    console.error('API连接检查失败:', error)
    return {
      code: 500,
      message: error.message || '无法连接到API服务器',
      error: error
    }
  })
}

// 调试用接口 - 获取当前环境配置
export function getEnvironmentConfig() {
  const config = {
    baseUrl: getEnvValue('VITE_BASE_API', '未设置'),
    env: getEnvValue('MODE', process.env.NODE_ENV || '未知'),
    buildTime: getEnvValue('VITE_BUILD_TIME', '未知'),
    apiVersion: getEnvValue('VITE_API_VERSION', '未知')
  }
  
  console.log('当前环境配置:', config)
  return Promise.resolve({
    code: 200,
    data: config,
    message: '获取环境配置成功'
  })
}

// 调试用接口 - 测试档案列表请求
export function testArchiveListRequest(params) {
  // 使用直接的axios请求，绕过request工具
  const baseUrl = getEnvValue('VITE_BASE_API', '');
  const token = localStorage.getItem('token');
  const apiUrl = `${baseUrl}/archives`;
  
  console.log('直接测试API请求:', apiUrl);
  console.log('请求参数:', params);
  console.log('Authorization token:', token ? `Bearer ${token}` : '未设置');
  
  return axios({
    url: apiUrl,
    method: 'get',
    params,
    timeout: 30000,
    headers: token ? { 'Authorization': `Bearer ${token}` } : {}
  }).then(response => {
    return {
      code: 200,
      data: response.data,
      headers: response.headers,
      status: response.status,
      raw: response
    }
  }).catch(error => {
    return {
      code: 500,
      message: error.message,
      error: error,
      config: error.config,
      raw: error.response || error
    }
  })
} 