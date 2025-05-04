import request from '@/utils/request'

// 发送聊天消息
export function sendChatMessage(query, sessionId = null) {
  return request({
    url: '/api/chat/query',
    method: 'post',
    data: {
      query,
      sessionId
    }
  })
}

// 获取聊天历史
export function getChatHistory(limit = 10) {
  return request({
    url: '/api/chat/history',
    method: 'get',
    params: { limit }
  })
}

// 提交反馈
export function submitFeedback(chatId, feedback) {
  return request({
    url: `/api/chat/feedback/${chatId}`,
    method: 'post',
    data: feedback
  })
}

// 获取会话列表
export function getChatSessions() {
  return request({
    url: '/api/chat/sessions',
    method: 'get'
  })
}

// 创建新会话
export function createChatSession(title = null) {
  return request({
    url: '/api/chat/session',
    method: 'post',
    data: { title }
  })
}

// 更新会话
export function updateChatSession(sessionId, sessionData) {
  return request({
    url: `/api/chat/session/${sessionId}`,
    method: 'put',
    data: sessionData
  })
}

// 删除会话
export function deleteChatSession(sessionId) {
  return request({
    url: `/api/chat/session/${sessionId}`,
    method: 'delete'
  })
}

// 获取会话历史消息
export function getSessionHistory(sessionId) {
  return request({
    url: `/api/chat/session/${sessionId}`,
    method: 'get'
  })
}

// 获取聊天统计数据
export function getChatStatistics() {
  return request({
    url: '/api/chat/statistics',
    method: 'get'
  })
} 