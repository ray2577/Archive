<template>
  <div class="ai-chat-view">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <h2>AI档案助手</h2>
          <el-tooltip content="AI助手可以帮助您快速查找档案、回答问题">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </template>
      <AiChatBot />
    </el-card>
  </div>
</template>

<script setup>
import { QuestionFilled } from '@element-plus/icons-vue'
import AiChatBot from '@/components/AiChatBot.vue'
</script>

<style scoped>
.ai-chat-view {
  padding: 20px;
  height: 100%;
}

.chat-card {
  height: calc(100vh - 140px);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-header h2 {
  margin: 0;
}
</style> 