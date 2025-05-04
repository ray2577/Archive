<template>
  <div class="chat-container">
    <!-- 加载动画 -->
    <div v-if="initialLoading" class="loading-overlay">
      <el-progress type="circle" :percentage="loadingProgress"></el-progress>
      <p>正在加载对话历史...</p>
    </div>

    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">
      <div v-if="showScrollToBottom" class="scroll-to-bottom" @click="scrollToBottom">
        <el-icon><ArrowDown /></el-icon>
      </div>

      <div v-for="(message, index) in displayedMessages" :key="index" 
           :class="['message', message.type, {'with-avatar': true}]">
        <!-- 消息头部：时间和操作 -->
        <div class="message-header">
          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
          <div class="message-actions" v-if="message.type === 'bot'">
            <el-tooltip content="复制回答">
              <el-icon @click="copyMessage(message)"><CopyDocument /></el-icon>
            </el-tooltip>
            <el-tooltip content="朗读回答">
              <el-icon @click="speakMessage(message)"><Microphone /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <!-- 消息内容 -->
        <div class="message-content">
          <img :src="message.type === 'user' ? userAvatar : botAvatar" class="avatar" />
          <div class="content">
            <div v-if="message.type === 'user'" class="user-query">{{ message.content }}</div>
            <div v-else>
              <div v-if="message.isTyping" class="typing-animation">
                <span></span><span></span><span></span>
              </div>
              <div v-else>
                <div class="bot-response" v-html="formatMessage(message.content)"></div>
                
                <!-- 相关档案列表 -->
                <div v-if="message.archives?.length" class="archives-list">
                  <h4>相关档案：</h4>
                  <el-collapse>
                    <el-collapse-item v-for="archive in message.archives" :key="archive.id">
                      <template #title>
                        <el-icon><Document /></el-icon>
                        {{ archive.title }}
                      </template>
                      <div class="archive-details">
                        <p><strong>档案编号：</strong>{{ archive.fileNumber }}</p>
                        <p><strong>创建时间：</strong>{{ formatDate(archive.createTime) }}</p>
                        <p><strong>存放位置：</strong>{{ archive.location }}</p>
                        <p><strong>状态：</strong>{{ formatStatus(archive.status) }}</p>
                        <el-button type="primary" size="small" @click="viewArchive(archive)">
                          查看详情
                        </el-button>
                      </div>
                    </el-collapse-item>
                  </el-collapse>
                </div>

                <!-- 反馈按钮 -->
                <div class="feedback-buttons" v-if="!message.feedbackProvided">
                  <el-rate v-model="message.relevanceScore" :max="5"></el-rate>
                  <el-button-group>
                    <el-button type="success" size="small" @click="provideFeedback(message, true)">
                      <el-icon>
                        <!-- <ThumbUp /> -->
                        </el-icon>有帮助
                    </el-button>
                    <el-button type="danger" size="small" @click="provideFeedback(message, false)">
                      <el-icon>
                        <!-- <ThumbDown /> -->
                        </el-icon>需改进
                    </el-button>
                  </el-button-group>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷回复区域 -->
    <div v-if="quickReplies.length" class="quick-replies">
      <el-tag
        v-for="reply in quickReplies"
        :key="reply"
        @click="handleQuickReply(reply)"
        class="quick-reply"
      >
        {{ reply }}
      </el-tag>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <div class="input-actions">
        <el-tooltip content="语音输入">
          <el-button circle @click="startVoiceInput" :loading="isListening">
            <el-icon><Microphone /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="上传文件">
          <el-upload action="" :auto-upload="false" :show-file-list="false" @change="handleFileUpload">
            <el-button circle>
              <el-icon><Upload /></el-icon>
            </el-button>
          </el-upload>
        </el-tooltip>
      </div>

      <el-input
        v-model="query"
        type="textarea"
        :rows="2"
        :placeholder="inputPlaceholder"
        @keyup.enter.exact="handleEnter"
        @keyup.ctrl.enter="handleEnter"
        @input="handleInput"
        ref="queryInput"
      >
        <template #append>
          <el-button type="primary" @click="sendQuery" :loading="loading">
            发送
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 档案预览对话框 -->
    <el-dialog
      v-model="archiveDialogVisible"
      :title="selectedArchive?.title"
      width="60%"
      class="archive-dialog"
    >
      <div v-if="selectedArchive" class="archive-preview">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="档案编号">{{ selectedArchive.fileNumber }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedArchive.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="存放位置">{{ selectedArchive.location }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ formatStatus(selectedArchive.status) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ selectedArchive.description }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="archive-actions">
          <el-button type="primary" @click="downloadArchive(selectedArchive)">下载</el-button>
          <el-button type="success" @click="borrowArchive(selectedArchive)">申请借阅</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowDown, Document, CopyDocument, Microphone,  Upload
} from '@element-plus/icons-vue'
import { useSpeechRecognition } from '@vueuse/core'

// 使用内联 base64 编码的图片代替文件导入
const userAvatarImg = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCIgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiBmaWxsPSIjMDA3NEQ5Ij48cGF0aCBkPSJNMjQgOGM0LjQgMCA4IDMuNiA4IDhzLTMuNiA4LTggOC04LTMuNi04LTggMy42LTggOC04em0wIDIwYzguOCAwIDE2IDQgMTYgOHYzSDb2LTNjMC00IDcuMi04IDE2LTh6Ii8+PC9zdmc+'
const botAvatarImg = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCIgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiBmaWxsPSIjNDJCODgzIj48cGF0aCBkPSJNMzEgMjJoM3YzaC0zdi0zem0tMTcgMGgzdjNoLTN2LTN6bTE3LjQ1LTYuMzZBMTAuODggMTAuODggMCAwIDEgMzYgMjQuNXYyaC0ydi0yYzAtNS4xLTQuMTYtOS4yNC05LjIzLTkuMjQtNC43NCAwLTguNjYgMy42LTkuMTUgOC4zaC0yYy41LTUuOTQgNS40LTEwLjMgMTEuMTUtMTAuM2E5Ljc1IDkuNzUgMCAwIDEgNC43OCAxLjJjLjIxLjExLjQuMjUuNi4zOC4wNC4wMy4wOC4wNS4xMi4wOGExMS4xNyAxMS4xNyAwIDAgMSAyLjE1IDEuNzNjLjExLjEuMjEuMi4zMS4zMnpNMjQgNDNjMi4yMSAwIDQtMS43OSA0LTRoLThjMCAxLjc5IDEuNzkgNCA0IDR6TTM2IDE4LjVDMzYgMTEuMDMgMzAuMDcgNS4xIDIyLjYgNS4xYy03LjQ4IDAtMTMuNCA2LTEzLjQgMTMuNGMwIDQuNiAyLjE1IDguNjUgNS45MiAxMS42OEwxNS40IDQwaDE3LjJsMi44OC05LjUzYzMuNzctMy4wNCA1LjUyLTYuOTcgNS41Mi0xMS45N3oiLz48L3N2Zz4='

// 类型定义
interface Archive {
  id: number;
  title: string;
  fileNumber: string;
  createTime: string;
  location: string;
  status: string;
  description: string;
}

interface Message {
  type: 'user' | 'bot';
  content: string;
  timestamp: number;
  isTyping?: boolean;
  archives?: Archive[];
  chatId?: number;
  feedbackProvided?: boolean;
  relevanceScore?: number;
}

interface ApiResponse {
  answer: string;
  relevantArchives: Archive[];
  recommendations: string[];
  chatId: number;
  error?: string;
}

// 常量配置
const API_ENDPOINTS = {
  QUERY: '/api/ai-chat/query',
  FEEDBACK: '/api/chat/feedback',
  HISTORY: '/api/chat/history'
} as const;

const MAX_RETRIES = 3;
const RETRY_DELAY = 1000;

// 状态变量
const query = ref('')
const loading = ref(false)
const initialLoading = ref(true)
const loadingProgress = ref(0)
const messages = ref<Message[]>([])
const displayedMessages = ref<Message[]>([])
const messagesContainer = ref<HTMLElement | null>(null)
const showScrollToBottom = ref(false)
const archiveDialogVisible = ref(false)
const selectedArchive = ref<Archive | null>(null)
const quickReplies = ref<string[]>([])
const isListening = ref(false)
const inputPlaceholder = ref('输入您的问题，或点击语音输入...')
const userAvatar = ref(userAvatarImg)
const botAvatar = ref(botAvatarImg)

// API 调用工具函数
const fetchWithRetry = async (url: string, options: RequestInit, retries = MAX_RETRIES): Promise<any> => {
  try {
    const response = await fetch(url, options);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    if (data.error) {
      throw new Error(data.error);
    }
    return data;
  } catch (error) {
    if (retries > 0) {
      await new Promise(resolve => setTimeout(resolve, RETRY_DELAY));
      return fetchWithRetry(url, options, retries - 1);
    }
    throw error;
  }
};

// 语音识别
const { isSupported, start, stop, result } = useSpeechRecognition({
  lang: 'zh-CN',
  continuous: true,
})

// 监听语音识别结果
watch(result, (value) => {
  if (value) {
    query.value = value
  }
})

// 处理滚动
const handleScroll = () => {
  if (!messagesContainer.value) return
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
  showScrollToBottom.value = scrollHeight - scrollTop - clientHeight > 100
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化消息内容
const formatMessage = (content) => {
  // 处理Markdown格式
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/\n/g, '<br>')
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour12: false })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    AVAILABLE: '可借阅',
    BORROWED: '已借出',
    PROCESSING: '处理中'
  }
  return statusMap[status] || status
}

// 复制消息内容
const copyMessage = async (message) => {
  try {
    await navigator.clipboard.writeText(message.content)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// 语音朗读
const speakMessage = (message) => {
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(message.content)
    utterance.lang = 'zh-CN'
    speechSynthesis.speak(utterance)
  } else {
    ElMessage.warning('您的浏览器不支持语音朗读')
  }
}

// 开始语音输入
const startVoiceInput = async () => {
  if (!isSupported.value) {
    ElMessage.warning('您的浏览器不支持语音输入')
    return
  }
  
  try {
    isListening.value = true
    await start()
  } catch (err) {
    ElMessage.error('无法启动语音输入')
    isListening.value = false
  }
}

// 处理文件上传
const handleFileUpload = (file) => {
  // 实现文件上传逻辑
}

// 处理快捷回复
const handleQuickReply = (reply) => {
  query.value = reply
  sendQuery()
}

// 处理输入
const handleInput = () => {
  // 实现输入建议
}

// 处理回车
const handleEnter = (e) => {
  if (e.ctrlKey || !e.shiftKey) {
    e.preventDefault()
    sendQuery()
  }
}

// 发送查询
const sendQuery = async () => {
  if (!query.value.trim() || loading.value) return;
  
  const userMessage: Message = {
    type: 'user',
    content: query.value,
    timestamp: Date.now()
  };
  
  const botMessage: Message = {
    type: 'bot',
    isTyping: true,
    content: '',
    timestamp: Date.now()
  };
  
  messages.value.push(userMessage);
  messages.value.push(botMessage);
  
  await scrollToBottom();
  
  try {
    loading.value = true;
    const data = await fetchWithRetry(API_ENDPOINTS.QUERY, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: query.value })
    });
    
    // 更新机器人消息
    botMessage.isTyping = false;
    botMessage.content = data.answer;
    botMessage.archives = data.relevantArchives;
    botMessage.chatId = data.chatId;
    
    // 更新快捷回复
    quickReplies.value = data.recommendations || [];
    
  } catch (error) {
    botMessage.isTyping = false;
    botMessage.content = '抱歉，处理您的问题时出现了错误，请稍后重试。';
    console.error('Query error:', error);
    ElMessage.error(error instanceof Error ? error.message : '请求失败');
  } finally {
    loading.value = false;
    query.value = '';
    await scrollToBottom();
  }
};

// 提供反馈
const provideFeedback = async (message: Message, isHelpful: boolean) => {
  if (!message.chatId) {
    ElMessage.error('无法提交反馈：会话ID不存在');
    return;
  }

  try {
    await fetchWithRetry(`${API_ENDPOINTS.FEEDBACK}/${message.chatId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        isHelpful,
        relevanceScore: message.relevanceScore,
        userAction: 'FEEDBACK'
      })
    });
    
    message.feedbackProvided = true;
    ElMessage.success('感谢您的反馈！');
  } catch (error) {
    console.error('Feedback error:', error);
    ElMessage.error('反馈提交失败');
  }
};

// 查看档案详情
const viewArchive = (archive) => {
  selectedArchive.value = archive
  archiveDialogVisible.value = true
}

// 下载档案
const downloadArchive = async (archive) => {
  // 实现下载逻辑
}

// 申请借阅
const borrowArchive = async (archive) => {
  // 实现借阅逻辑
}

// 加载历史记录
const loadChatHistory = async () => {
  try {
    const response = await fetch(API_ENDPOINTS.HISTORY)
    const data = await response.json()
    
    messages.value = data.map(item => ({
      type: item.query ? 'user' : 'bot',
      content: item.query || item.response,
      timestamp: new Date(item.createTime).getTime(),
      chatId: item.id,
      archives: item.relevantArchives
    }))
    
    await scrollToBottom()
  } catch (error) {
    console.error('Error loading chat history:', error)
    ElMessage.error('加载历史记录失败')
  }
}

// 组件挂载
onMounted(async () => {
  // 模拟加载进度
  const interval = setInterval(() => {
    loadingProgress.value += 10
    if (loadingProgress.value >= 100) {
      clearInterval(interval)
      initialLoading.value = false
    }
  }, 200)
  
  await loadChatHistory()
  
  // 添加欢迎消息
  if (messages.value.length === 0) {
    messages.value.push({
      type: 'bot',
      content: '您好！我是AI档案助手，很高兴为您服务。您可以：\n' +
               '1. 搜索特定档案\n' +
               '2. 查询档案统计信息\n' +
               '3. 申请档案借阅\n' +
               '4. 或者问我任何关于档案的问题',
      timestamp: Date.now()
    })
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  position: relative;
}

.scroll-to-bottom {
  position: fixed;
  bottom: 100px;
  right: 20px;
  background: var(--el-color-primary);
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.scroll-to-bottom:hover {
  transform: translateY(-2px);
}

.message {
  margin-bottom: 20px;
  opacity: 0;
  animation: fadeIn 0.3s forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-actions {
  display: flex;
  gap: 8px;
}

.message-actions .el-icon {
  cursor: pointer;
  color: var(--el-text-color-secondary);
  transition: color 0.3s;
}

.message-actions .el-icon:hover {
  color: var(--el-color-primary);
}

.message-content {
  display: flex;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.content {
  flex: 1;
  max-width: calc(100% - 52px);
}

.user-query {
  background: var(--el-color-primary-light-9);
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 4px;
}

.bot-response {
  background: white;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.typing-animation {
  display: flex;
  gap: 4px;
  padding: 12px;
}

.typing-animation span {
  width: 8px;
  height: 8px;
  background: var(--el-color-primary);
  border-radius: 50%;
  animation: typing 1s infinite;
}

.typing-animation span:nth-child(2) { animation-delay: 0.2s; }
.typing-animation span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.archives-list {
  margin-top: 12px;
}

.archive-details {
  padding: 12px;
  background: var(--el-bg-color-page);
  border-radius: 4px;
}

.feedback-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.quick-replies {
  padding: 8px 20px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-reply {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-reply:hover {
  transform: translateY(-2px);
}

.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid var(--el-border-color-lighter);
}

.input-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.archive-dialog :deep(.el-dialog__body) {
  padding-top: 20px;
}

.archive-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 