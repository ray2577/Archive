<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="session-selector">
        <el-select 
          v-model="currentSessionId" 
          placeholder="选择会话" 
          @change="switchSession"
          class="session-select"
        >
          <el-option
            v-for="session in chatSessions"
            :key="session.id"
            :label="session.title"
            :value="session.id"
          />
        </el-select>
        <el-button type="primary" size="small" icon="Plus" @click="createNewSession" />
        <el-button 
          v-if="currentSessionId" 
          type="danger" 
          size="small" 
          icon="Delete" 
          @click="deleteCurrentSession" 
        />
      </div>
    </div>
    
    <div class="chat-messages" ref="chatMessages">
      <div v-for="(message, index) in messages" :key="index" class="message-container">
        <div :class="['message', message.isUser ? 'user-message' : 'ai-message']">
          <div class="message-header">
            <span class="message-sender">{{ message.isUser ? '您' : 'AI助手' }}</span>
            <span class="message-time">{{ formatTime(message.time) }}</span>
          </div>
          <div class="message-content">
            {{ message.content }}
          </div>
          
          <!-- 相关档案列表 (如果有) -->
          <div v-if="!message.isUser && message.relevantArchives && message.relevantArchives.length > 0" class="relevant-archives">
            <div class="relevant-archives-header">相关档案：</div>
            <div v-for="archive in message.relevantArchives" :key="archive.id" class="archive-item">
              <el-link type="primary" @click="viewArchive(archive.id)">
                {{ archive.title }} ({{ archive.fileNumber }})
              </el-link>
            </div>
          </div>
          
          <!-- 反馈按钮 (仅AI消息显示) -->
          <div v-if="!message.isUser" class="message-feedback">
            <el-button 
              size="small" 
              text
              :type="message.feedback === true ? 'primary' : 'default'"
              @click="provideFeedback(message.id, true)"
            >
              <el-icon><Thumb /></el-icon> 有帮助
            </el-button>
            <el-button 
              size="small"
              text
              :type="message.feedback === false ? 'danger' : 'default'"
              @click="provideFeedback(message.id, false)"
            >
              <el-icon><Thumb style="transform: rotate(180deg)" /></el-icon> 无帮助
            </el-button>
          </div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message-loading">
        <el-loading />
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入您的问题..."
        @keyup.enter.native="sendMessage"
      />
      <el-button 
        type="primary" 
        :disabled="!inputMessage.trim() || isLoading" 
        @click="sendMessage"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  sendChatMessage, 
  getChatSessions, 
  createChatSession, 
  deleteChatSession, 
  getSessionHistory,
  submitFeedback
} from '@/api/chat'

const router = useRouter()
const chatMessages = ref(null)

// Chat state
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)

// Session management
const chatSessions = ref([])
const currentSessionId = ref(null)

// Load chat sessions
const loadSessions = async () => {
  try {
    const res = await getChatSessions()
    chatSessions.value = res.data
    if (chatSessions.value.length > 0 && !currentSessionId.value) {
      currentSessionId.value = chatSessions.value[0].id
      await loadSessionMessages(currentSessionId.value)
    }
  } catch (error) {
    ElMessage.error('获取会话列表失败')
    console.error(error)
  }
}

// Load messages for a session
const loadSessionMessages = async (sessionId) => {
  if (!sessionId) return
  
  isLoading.value = true
  try {
    const res = await getSessionHistory(sessionId)
    messages.value = res.data.map(msg => ({
      id: msg.id,
      content: msg.isUser ? msg.query : msg.response,
      isUser: msg.isUser,
      time: new Date(msg.createTime),
      relevantArchives: msg.relevantArchives || [],
      feedback: msg.isHelpful
    }))
    
    // Scroll to bottom after messages load
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('获取会话消息失败')
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

// Switch between sessions
const switchSession = (sessionId) => {
  if (sessionId !== currentSessionId.value) {
    loadSessionMessages(sessionId)
  }
}

// Create a new chat session
const createNewSession = async () => {
  try {
    const res = await createChatSession()
    chatSessions.value.unshift(res.data)
    currentSessionId.value = res.data.id
    messages.value = []
  } catch (error) {
    ElMessage.error('创建新会话失败')
    console.error(error)
  }
}

// Delete current session
const deleteCurrentSession = async () => {
  if (!currentSessionId.value) return
  
  try {
    await deleteChatSession(currentSessionId.value)
    const index = chatSessions.value.findIndex(s => s.id === currentSessionId.value)
    if (index !== -1) {
      chatSessions.value.splice(index, 1)
    }
    
    // Switch to another session if available
    if (chatSessions.value.length > 0) {
      currentSessionId.value = chatSessions.value[0].id
      await loadSessionMessages(currentSessionId.value)
    } else {
      currentSessionId.value = null
      messages.value = []
    }
  } catch (error) {
    ElMessage.error('删除会话失败')
    console.error(error)
  }
}

// Send a message
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  // Add user message to the chat
  const userMessage = {
    content: inputMessage.value,
    isUser: true,
    time: new Date()
  }
  messages.value.push(userMessage)
  
  // Clear input and scroll down
  const messageText = inputMessage.value
  inputMessage.value = ''
  await nextTick()
  scrollToBottom()
  
  // Set loading state
  isLoading.value = true
  
  try {
    // Send message to backend
    const res = await sendChatMessage(messageText, currentSessionId.value)
    
    // If this is a new conversation, update session ID
    if (!currentSessionId.value) {
      currentSessionId.value = res.data.sessionId
      // Reload sessions to get the new one
      await loadSessions()
    }
    
    // Add AI response to chat
    messages.value.push({
      id: res.data.chatId,
      content: res.data.response,
      isUser: false,
      time: new Date(),
      relevantArchives: res.data.relevantArchives || [],
      feedback: null
    })
    
    // Scroll to new message
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送消息失败')
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

// Provide feedback for a message
const provideFeedback = async (messageId, isHelpful) => {
  if (!messageId) return
  
  const message = messages.value.find(m => m.id === messageId)
  if (!message) return
  
  try {
    await submitFeedback(messageId, { isHelpful })
    message.feedback = isHelpful
  } catch (error) {
    ElMessage.error('提交反馈失败')
    console.error(error)
  }
}

// View archive details
const viewArchive = (archiveId) => {
  router.push(`/archive/detail/${archiveId}`)
}

// Format time for display
const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Auto-scroll to bottom of chat
const scrollToBottom = () => {
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}

// Initialize
onMounted(() => {
  loadSessions()
})

// Watch for session changes
watch(currentSessionId, (newSessionId) => {
  if (newSessionId) {
    loadSessionMessages(newSessionId)
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.chat-header {
  padding: 12px;
  border-bottom: 1px solid #e0e0e0;
  background-color: #fff;
}

.session-selector {
  display: flex;
  align-items: center;
}

.session-select {
  flex-grow: 1;
  margin-right: 10px;
}

.chat-messages {
  flex-grow: 1;
  padding: 16px;
  overflow-y: auto;
  height: 500px;
}

.message-container {
  margin-bottom: 16px;
}

.message {
  max-width: 80%;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.user-message {
  margin-left: auto;
  background-color: #e1f3ff;
  border: 1px solid #b3e0ff;
}

.ai-message {
  margin-right: auto;
  background-color: white;
  border: 1px solid #e0e0e0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
  color: #666;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.message-loading {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.relevant-archives {
  margin-top: 12px;
  font-size: 13px;
  border-top: 1px solid #e0e0e0;
  padding-top: 8px;
}

.relevant-archives-header {
  font-weight: bold;
  margin-bottom: 6px;
}

.archive-item {
  margin-bottom: 4px;
}

.message-feedback {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 12px;
}

.chat-input {
  display: flex;
  padding: 12px;
  border-top: 1px solid #e0e0e0;
  background-color: #fff;
}

.chat-input .el-input {
  margin-right: 12px;
}
</style> 