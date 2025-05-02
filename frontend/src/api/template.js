import request from '@/utils/request'

/**
 * 获取模板列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.name - 模板名称，用于搜索
 * @returns {Promise} 返回请求的Promise对象
 */
export function getTemplateList(params) {
  return request({
    url: '/api/templates',
    method: 'get',
    params
  })
}

/**
 * 获取模板详情
 * @param {number} id - 模板ID
 * @returns {Promise} 返回请求的Promise对象
 */
export function getTemplateDetail(id) {
  return request({
    url: `/api/templates/${id}`,
    method: 'get'
  })
}

/**
 * 创建新模板
 * @param {Object} data - 模板数据
 * @returns {Promise} 返回请求的Promise对象
 */
export function createTemplate(data) {
  return request({
    url: '/api/templates',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新模板
 * @param {number} id - 模板ID
 * @param {Object} data - 模板数据
 * @returns {Promise} 返回请求的Promise对象
 */
export function updateTemplate(id, data) {
  return request({
    url: `/api/templates/${id}`,
    method: 'put',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除模板
 * @param {number} id - 模板ID
 * @returns {Promise} 返回请求的Promise对象
 */
export function deleteTemplate(id) {
  return request({
    url: `/api/templates/${id}`,
    method: 'delete'
  })
}

/**
 * 获取模板预览URL
 * @param {number} id - 模板ID
 * @returns {Promise} 返回请求的Promise对象
 */
export function getTemplatePreviewUrl(id) {
  return request({
    url: `/api/templates/${id}/preview`,
    method: 'get'
  })
}

/**
 * 下载模板文件
 * @param {number} id - 模板ID
 * @returns {Promise} 返回请求的Promise对象
 */
export function downloadTemplate(id) {
  return request({
    url: `/api/templates/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
} 