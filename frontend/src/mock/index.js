// // 模拟数据服务
// import axios from 'axios'
// import { getArchiveList } from '@/api/archive'

// // 拦截 API 请求，返回模拟数据
// const mock = {
//   setup() {
//     console.log('设置模拟数据服务')
    
//     // 添加特定模拟数据
//     // 借阅记录模拟数据
//     if (!handleMockResponse.borrowRecords) {
//       handleMockResponse.borrowRecords = Array.from({ length: 50 }, (_, i) => ({
//         id: i + 1,
//         archiveId: Math.floor(Math.random() * 100) + 1,
//         archiveCode: `ARC${String(Math.floor(Math.random() * 1000)).padStart(4, '0')}`,
//         archiveName: ['财务报表', '人事档案', '会议纪要', '合同文本', '技术文档'][Math.floor(Math.random() * 5)],
//         borrower: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
//         department: ['财务部', '人事部', '行政部', '技术部', '市场部'][Math.floor(Math.random() * 5)],
//         borrowDate: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
//         returnDate: new Date(Date.now() + Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
//         actualReturnDate: Math.random() > 0.5 ? new Date(Date.now() - Math.floor(Math.random() * 15 * 24 * 60 * 60 * 1000)).toISOString() : null,
//         status: Math.random() > 0.5 ? 1 : 2
//       }))
//     }
//   }
// }

// // 在api/request.js中已经有处理模拟数据的函数，这里添加更多特定的模拟数据
// window.handleMockResponse = function(config) {
//   console.log('使用模拟数据', config.url)
  
//   // 档案统计信息 
//   if (config.url === '/statistics/overview' && config.method === 'get') {
//     return {
//       code: 200,
//       message: '获取统计数据成功',
//       data: {
//         totalCount: 1258,
//         activeCount: 1226,
//         borrowedCount: 32,
//         monthlyNew: 87,
//         userCount: 45,
//         borrowTypeDistribution: [
//           { type: '财务档案', value: 42 },
//           { type: '人事档案', value: 28 },
//           { type: '项目档案', value: 18 },
//           { type: '合同档案', value: 10 },
//           { type: '其他档案', value: 2 }
//         ],
//         monthlyTrends: Array.from({ length: 12 }, (_, i) => ({
//           month: `${i + 1}月`,
//           new: Math.floor(Math.random() * 30 + 60),
//           borrow: Math.floor(Math.random() * 20 + 40)
//         }))
//       }
//     }
//   }

//   // 借阅记录API模拟数据
//   if (config.url === '/archive/borrow/records' && config.method === 'get') {
//     const { page = 1, pageSize = 10 } = config.params || {}
//     const startIndex = (page - 1) * pageSize
//     const endIndex = startIndex + pageSize
    
//     return {
//       code: 200,
//       message: '获取借阅记录成功',
//       data: {
//         total: handleMockResponse.borrowRecords.length,
//         list: handleMockResponse.borrowRecords.slice(startIndex, endIndex)
//       }
//     }
//   }
  
//   // 归还档案API模拟数据
//   if (config.url.includes('/archive/return/') && config.method === 'put') {
//     const id = parseInt(config.url.split('/').pop())
//     const recordIndex = handleMockResponse.borrowRecords.findIndex(record => record.id === id)
    
//     if (recordIndex !== -1) {
//       handleMockResponse.borrowRecords[recordIndex].status = 1 // 已归还
//       handleMockResponse.borrowRecords[recordIndex].actualReturnDate = new Date().toISOString()
//     }
    
//     return {
//       code: 200,
//       message: '归还档案成功',
//       data: {}
//     }
//   }
  
//   // 档案搜索API模拟数据
//   if (config.url === '/archive/search' && config.method === 'post') {
//     const { page = 1, pageSize = 10 } = config.params || {}
    
//     return {
//       code: 200,
//       message: '搜索成功',
//       data: {
//         total: 85,
//         list: Array.from({ length: Math.min(pageSize, 85) }, (_, i) => ({
//           id: (page - 1) * pageSize + i + 1,
//           code: `ARC${String((page - 1) * pageSize + i + 1000).padStart(4, '0')}`,
//           name: ['财务报表', '人事档案', '会议纪要', '合同文本', '技术文档'][Math.floor(Math.random() * 5)] + ` ${(page - 1) * pageSize + i + 1}`,
//           type: String((Math.floor(Math.random() * 5) + 1)),
//           location: `${String.fromCharCode(65 + Math.floor(Math.random() * 3))}${Math.floor(Math.random() * 3) + 1}`,
//           responsible: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
//           fileSize: Math.floor(Math.random() * 10000000),
//           borrowCount: Math.floor(Math.random() * 20),
//           status: Math.floor(Math.random() * 2) + 1,
//           createTime: new Date(Date.now() - Math.floor(Math.random() * 10000000000)).toISOString(),
//           content: '这是档案的详细内容，包含了相关的关键信息...',
//           keywords: ['关键词1', '关键词2', '关键词3'].slice(0, Math.floor(Math.random() * 3) + 1)
//         }))
//       }
//     }
//   }
  
//   // 用户列表API模拟数据
//   if (config.url === '/system/users' && config.method === 'get') {
//     const { page = 1, pageSize = 10 } = config.params || {}
    
//     return {
//       code: 200,
//       message: '获取用户列表成功',
//       data: {
//         total: 45,
//         list: Array.from({ length: Math.min(pageSize, 45 - (page - 1) * pageSize) }, (_, i) => ({
//           id: (page - 1) * pageSize + i + 1,
//           username: `user${(page - 1) * pageSize + i + 1}`,
//           nickname: ['管理员', '档案管理员', '借阅人员', '普通用户'][Math.floor(Math.random() * 4)],
//           email: `user${(page - 1) * pageSize + i + 1}@example.com`,
//           phone: `1${Math.floor(Math.random() * 10)}${String(Math.floor(Math.random() * 100000000)).padStart(8, '0')}`,
//           role: ['admin', 'manager', 'user'][Math.floor(Math.random() * 3)],
//           status: Math.random() > 0.2 ? 1 : 0,
//           lastLogin: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
//           createTime: new Date(Date.now() - Math.floor(Math.random() * 365 * 24 * 60 * 60 * 1000)).toISOString()
//         }))
//       }
//     }
//   }
  
//   return null // 返回null表示没有特定的模拟数据，交由request.js处理
// }

// // 初始化模拟数据
// //mock.setup()
// mock.restore();


// export default mock 