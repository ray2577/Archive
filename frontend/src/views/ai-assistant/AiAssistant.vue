<template>
  <div class="ai-assistant-container">
    <div class="page-header">
      <h2>AI档案助手</h2>
      <div class="header-actions">
        <el-button type="primary" plain @click="clearHistory">
          <el-icon><Delete /></el-icon>
          清空历史
        </el-button>
        <el-button type="primary" @click="showHelp">
          <el-icon><QuestionFilled /></el-icon>
          使用帮助
        </el-button>
      </div>
    </div>
    
    <div class="assistant-content">
      <AiChatBot ref="chatbotRef" />
    </div>
    
    <el-dialog
      v-model="helpDialogVisible"
      title="AI档案助手使用指南"
      width="600px"
    >
      <div class="help-content">
        <h3>功能介绍</h3>
        <p>AI档案助手可以帮助您完成以下任务：</p>
        <ul>
          <li>搜索特定档案信息</li>
          <li>查询档案统计数据</li>
          <li>获取档案借阅指南</li>
          <li>解答档案管理相关问题</li>
          <li>提供档案工作流程建议</li>
        </ul>
        
        <h3>示例问题</h3>
        <el-tag 
          v-for="(question, index) in exampleQuestions" 
          :key="index"
          class="example-question"
          @click="askQuestion(question)"
        >
          {{ question }}
        </el-tag>
        
        <h3>使用技巧</h3>
        <ol>
          <li>问题尽量具体明确，可以帮助系统更准确地返回答案</li>
          <li>可以上传相关文件，AI会分析其中内容</li>
          <li>点击回答中的档案可以查看详细信息</li>
          <li>提供反馈有助于系统改进回答质量</li>
          <li>使用语音输入功能可以提高效率</li>
        </ol>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="helpDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="startTutorial">
            开始教程
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Delete, QuestionFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import AiChatBot from '@/components/AiChatBot.vue'

const chatbotRef = ref(null)
const helpDialogVisible = ref(false)

// 示例问题
const exampleQuestions = [
  '如何查找2023年的财务档案？',
  '帮我查找关于市场调研的档案',
  '怎样申请借阅档案？',
  '最近一周新增了哪些档案？',
  '档案编号AR20230012的详细信息',
  '谁借阅了人力资源管理制度文件？'
]

// 清空聊天历史
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有聊天历史记录吗？此操作不可恢复。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用聊天组件的清空方法
    if (chatbotRef.value) {
      chatbotRef.value.clearChatHistory()
    }
  } catch {
    // 用户取消操作
  }
}

// 显示帮助对话框
const showHelp = () => {
  helpDialogVisible.value = true
}

// 开始新手教程
const startTutorial = () => {
  helpDialogVisible.value = false
  
  // 这里可以实现分步引导教程
  // 例如使用 Element Plus 的 Tour 组件或第三方引导库
}

// 询问示例问题
const askQuestion = (question) => {
  helpDialogVisible.value = false
  
  // 调用聊天组件的提问方法
  if (chatbotRef.value) {
    chatbotRef.value.setQuery(question)
    chatbotRef.value.sendQuery()
  }
}
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
  background-color: #f5f7f9;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.assistant-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.help-content {
  padding: 10px;
}

.help-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 500;
}

.help-content p {
  margin: 10px 0;
}

.help-content ul,
.help-content ol {
  padding-left: 20px;
  margin: 10px 0;
}

.help-content li {
  margin-bottom: 8px;
}

.example-question {
  margin: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.example-question:hover {
  background-color: var(--el-color-primary-light-8);
  transform: translateY(-2px);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 