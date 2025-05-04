<template>
  <div class="request-retry-button">
    <el-button
      :type="type"
      :size="size"
      :loading="loading"
      @click="handleRetry"
      :disabled="loading || disabled"
    >
      <el-icon v-if="icon && !loading"><component :is="icon" /></el-icon>
      <slot>{{ text || '重试' }}</slot>
      <span v-if="counter > 0" class="retry-counter">({{ counter }})</span>
    </el-button>
    
    <div v-if="showStatus && lastStatus" :class="['status-message', `status-${lastStatus.success ? 'success' : 'error'}`]">
      {{ lastStatus.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { requestWithRetry } from '@/utils/request'

const props = defineProps({
  // 请求配置，与axios配置相同
  request: {
    type: Object,
    required: true
  },
  // 重试次数上限
  maxRetries: {
    type: Number,
    default: 3
  },
  // 重试间隔（毫秒）
  retryDelay: {
    type: Number,
    default: 1000
  },
  // 请求成功回调
  onSuccess: {
    type: Function,
    default: null
  },
  // 请求失败回调
  onError: {
    type: Function,
    default: null
  },
  // 按钮文本
  text: {
    type: String,
    default: '重试'
  },
  // 按钮类型
  type: {
    type: String,
    default: 'primary'
  },
  // 按钮尺寸
  size: {
    type: String,
    default: 'default'
  },
  // 图标
  icon: {
    type: String,
    default: 'Refresh'
  },
  // 是否显示状态信息
  showStatus: {
    type: Boolean,
    default: true
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  }
})

// 发射事件
const emit = defineEmits(['success', 'error', 'retry'])

const loading = ref(false)
const counter = ref(0)
const lastStatus = ref(null)

// 处理重试
async function handleRetry() {
  if (loading.value) return
  
  loading.value = true
  counter.value += 1
  
  try {
    emit('retry', { attempt: counter.value, config: props.request })
    
    const response = await requestWithRetry(
      props.request,
      props.maxRetries,
      props.retryDelay
    )
    
    // 成功处理
    lastStatus.value = {
      success: true,
      message: '请求成功',
      data: response.data
    }
    
    if (props.onSuccess) {
      props.onSuccess(response)
    }
    
    emit('success', {
      response,
      attempt: counter.value
    })
    
    ElMessage.success('请求成功')
  } catch (error) {
    // 失败处理
    lastStatus.value = {
      success: false,
      message: error.message || '请求失败',
      error
    }
    
    if (props.onError) {
      props.onError(error)
    }
    
    emit('error', {
      error,
      attempt: counter.value
    })
    
    ElMessage.error(`请求失败: ${error.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.request-retry-button {
  display: inline-block;
}

.retry-counter {
  margin-left: 4px;
  font-size: 12px;
  opacity: 0.8;
}

.status-message {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.2;
}

.status-success {
  color: var(--el-color-success);
}

.status-error {
  color: var(--el-color-danger);
}
</style> 