import request from '@/utils/request'

// 获取档案列表
export function getArchiveList(params) {
  return request({
    url: '/archive/list',
    method: 'get',
    params
  })
}

// 获取档案详情
export function getArchiveDetail(id) {
  return request({
    url: `/archive/detail/${id}`,
    method: 'get'
  })
}

// 创建档案
export function createArchive(data) {
  return request({
    url: '/archive/create',
    method: 'post',
    data
  })
}

// 更新档案
export function updateArchive(id, data) {
  return request({
    url: `/archive/update/${id}`,
    method: 'put',
    data
  })
}

// 删除档案
export function deleteArchive(id) {
  return request({
    url: `/archive/delete/${id}`,
    method: 'delete'
  })
}

// 获取借阅记录
export function getBorrowRecords(params) {
  return request({
    url: '/archive/borrow/records',
    method: 'get',
    params
  })
}

// 借阅档案
export function borrowArchive(data) {
  return request({
    url: '/archive/borrow',
    method: 'post',
    data
  })
}

// 归还档案
export function returnArchive(id) {
  return request({
    url: `/archive/return/${id}`,
    method: 'put'
  })
}

// 高级搜索
export function searchArchives(data, params) {
  return request({
    url: '/archive/search',
    method: 'post',
    data,
    params
  })
} 