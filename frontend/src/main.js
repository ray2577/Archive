import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './assets/main.css'
import { apiConfig } from './api/config'
import { isDevelopment, getMode } from './utils/env'

// 全局错误处理器
const handleGlobalError = (error, instance, info) => {
  console.error('全局错误:', error)
  console.error('错误来源:', info)
  console.error('组件实例:', instance)
  
  // 在这里可以添加错误上报逻辑
  // 例如发送到服务器或第三方监控服务
}

// 导入模拟数据（开发环境）
if (isDevelopment() && apiConfig.mockEnabled) {
  import('./mock').then(() => {
    console.log('模拟数据已加载')
  })
}

// 创建应用实例
const app = createApp(App)

// 全局错误处理
app.config.errorHandler = handleGlobalError

// 开发工具配置
if (isDevelopment()) {
  app.config.performance = true
  app.config.devtools = true
  
  // 打印Vue版本和环境信息
  console.log(`Vue版本: ${app.version}`)
  console.log(`运行环境: ${getMode()}`)
  console.log(`API基础路径: ${apiConfig.baseURL}`)
}

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

// 挂载应用
app.mount('#app')