<template>
  <div class="error-boundary">
    <template v-if="hasError">
      <div class="error-container">
        <el-result
          icon="error"
          title="组件渲染错误"
          sub-title="应用程序遇到了意外错误，请尝试重新加载页面或联系管理员。"
        >
          <template #extra>
            <div class="action-buttons">
              <el-button type="primary" @click="reload">刷新页面</el-button>
              <el-button @click="toggleDetails">
                {{ showDetails ? '隐藏详情' : '显示详情' }}
              </el-button>
              <el-button v-if="canRetry" @click="retry">重试</el-button>
            </div>
          </template>
          
          <div v-if="showDetails" class="error-details">
            <el-alert
              title="错误详情"
              type="error"
              :closable="false"
              show-icon
            >
              <p class="error-message">{{ error?.message || '未知错误' }}</p>
              <pre class="error-stack">{{ error?.stack }}</pre>
              
              <div v-if="componentTrace.length > 0" class="component-trace">
                <h4>组件追踪：</h4>
                <ul>
                  <li v-for="(component, index) in componentTrace" :key="index">
                    {{ component }}
                  </li>
                </ul>
              </div>
              
              <div class="error-info">
                <h4>错误信息：</h4>
                <p>时间: {{ errorTime }}</p>
                <p>URL: {{ currentUrl }}</p>
                <p>浏览器: {{ browserInfo }}</p>
              </div>
            </el-alert>
          </div>
        </el-result>
      </div>
    </template>
    
    <slot v-else></slot>
  </div>
</template>

<script setup>
import { ref, computed, onErrorCaptured, onMounted, provide } from 'vue'

// 错误状态
const error = ref(null)
const componentTrace = ref([])
const hasError = computed(() => !!error.value)
const showDetails = ref(false)
const errorTime = ref('')
const currentUrl = ref('')
const browserInfo = ref('')
const canRetry = ref(false)

// 记录组件追踪
function captureComponentTrace(err) {
  const trace = []
  
  if (err && err.component) {
    let current = err.component
    while (current) {
      if (current.type && current.type.name) {
        trace.push(current.type.name)
      } else if (current.type) {
        trace.push(String(current.type))
      }
      current = current.parent
    }
  }
  
  return trace
}

// 错误捕获处理函数
onErrorCaptured((err, instance, info) => {
  error.value = err
  componentTrace.value = captureComponentTrace({ component: instance })
  errorTime.value = new Date().toLocaleString()
  currentUrl.value = window.location.href
  browserInfo.value = navigator.userAgent
  canRetry.value = !!instance && typeof instance.proxy?.$forceUpdate === 'function'
  
  console.error('错误边界捕获到错误:', {
    error: err,
    instance,
    info,
    componentTrace: componentTrace.value
  })
  
  // 阻止错误向上传播
  return false
})

// 切换显示详情
function toggleDetails() {
  showDetails.value = !showDetails.value
}

// 重新加载页面
function reload() {
  window.location.reload()
}

// 尝试重试渲染
function retry() {
  if (!canRetry.value) return
  
  error.value = null
  componentTrace.value = []
  hasError.value = false
}

// 提供错误状态给子组件
provide('errorBoundary', {
  hasError,
  error,
  reportError: (err) => {
    error.value = err
    errorTime.value = new Date().toLocaleString()
  }
})

onMounted(() => {
  currentUrl.value = window.location.href
  browserInfo.value = navigator.userAgent
})
</script>

<style scoped>
.error-boundary {
  display: contents;
}

.error-container {
  padding: 20px;
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.error-details {
  margin-top: 20px;
  text-align: left;
}

.error-message {
  font-weight: bold;
  margin: 10px 0;
}

.error-stack {
  white-space: pre-wrap;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
  margin: 10px 0;
}

.component-trace, .error-info {
  margin-top: 15px;
}

.component-trace ul {
  padding-left: 20px;
}

.component-trace li, .error-info p {
  margin: 5px 0;
}
</style> 