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

// 获取工作流列表
export function getWorkflowList(params) {
  return enhancedRequest({
    url: '/workflows',
    method: 'get',
    params
  })
}

// 获取我的工作流列表
export function getMyWorkflows(params) {
  return enhancedRequest({
    url: '/workflows/my',
    method: 'get',
    params
  })
}

// 获取工作流详情
export function getWorkflowDetail(id) {
  return enhancedRequest({
    url: `/workflows/${id}`,
    method: 'get'
  })
}

// 创建新工作流
export function createWorkflow(data) {
  return enhancedRequest({
    url: '/workflows',
    method: 'post',
    data
  })
}

// 更新工作流
export function updateWorkflow(id, data) {
  return enhancedRequest({
    url: `/workflows/${id}`,
    method: 'put',
    data
  })
}

// 删除工作流
export function deleteWorkflow(id) {
  return enhancedRequest({
    url: `/workflows/${id}`,
    method: 'delete'
  })
}

// 启动工作流
export function startWorkflow(data) {
  return enhancedRequest({
    url: '/workflows/start',
    method: 'post',
    data
  })
}

// 处理工作流任务
export function completeTask(taskId, data) {
  return enhancedRequest({
    url: `/workflows/tasks/${taskId}/complete`,
    method: 'post',
    data
  })
}

// 获取待办任务列表
export function getTodoTasks(params) {
  return enhancedRequest({
    url: '/workflows/tasks/todo',
    method: 'get',
    params
  })
}

// 获取已办任务列表
export function getDoneTasks(params) {
  return enhancedRequest({
    url: '/workflows/tasks/done',
    method: 'get',
    params
  })
}

// 获取工作流历史记录
export function getWorkflowHistory(params) {
  return enhancedRequest({
    url: '/workflows/history',
    method: 'get',
    params
  })
}

// 获取工作流流程图
export function getWorkflowDiagram(processInstanceId) {
  return enhancedRequest({
    url: `/workflows/${processInstanceId}/diagram`,
    method: 'get',
    responseType: 'blob'
  })
}

// 驳回流程
export function rejectWorkflow(taskId, data) {
  return enhancedRequest({
    url: `/workflows/tasks/${taskId}/reject`,
    method: 'post',
    data
  })
}

// 转办任务
export function transferTask(taskId, data) {
  return enhancedRequest({
    url: `/workflows/tasks/${taskId}/transfer`,
    method: 'post',
    data
  })
}

// 委派任务
export function delegateTask(taskId, data) {
  return enhancedRequest({
    url: `/workflows/tasks/${taskId}/delegate`,
    method: 'post',
    data
  })
}

// 撤回流程
export function withdrawWorkflow(processInstanceId, data) {
  return enhancedRequest({
    url: `/workflows/${processInstanceId}/withdraw`,
    method: 'post',
    data
  })
}

// 获取流程定义列表
export function getProcessDefinitions(params) {
  return enhancedRequest({
    url: '/workflows/process-definitions',
    method: 'get',
    params
  })
}

// 导出工作流历史记录
export function exportWorkflowHistory(params) {
  return enhancedRequest({
    url: '/workflows/history/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 测试工作流API连接
export function testWorkflowHistoryRequest(params) {
  return enhancedRequest({
    url: '/workflows/history',
    method: 'get',
    params,
    timeout: 5000,
    validateStatus: status => true // 允许任何状态码以便查看错误详情
  })
}