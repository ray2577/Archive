<template>
  <div class="api-diagnostic-page">
    <el-page-header content="API诊断工具" @back="goBack" />
    
    <div class="page-content">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="tool-description">
            <el-alert
              title="API诊断工具使用说明"
              type="info"
              description="此工具可以帮助您诊断前端与后端API的连接问题，并提供可能的解决方案。如果您在使用系统时遇到了数据加载失败、请求错误等问题，可以使用此工具进行诊断。"
              show-icon
              :closable="false"
            />
          </div>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="mt-20">
        <el-col :span="24">
          <api-connection-test />
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="mt-20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <h3>环境信息</h3>
              </div>
            </template>
            
            <el-descriptions title="系统环境" :column="1" border>
              <el-descriptions-item label="当前环境">{{ currentEnv }}</el-descriptions-item>
              <el-descriptions-item label="API基础URL">{{ baseApiUrl }}</el-descriptions-item>
              <el-descriptions-item label="浏览器">{{ browserInfo }}</el-descriptions-item>
              <el-descriptions-item label="当前页面">{{ currentPage }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="mt-20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <h3>常见问题排查</h3>
              </div>
            </template>
            
            <el-collapse>
              <el-collapse-item title="1. API请求失败" name="1">
                <ul class="troubleshooting-list">
                  <li>检查网络连接是否正常</li>
                  <li>确认后端服务是否已启动</li>
                  <li>检查Vite配置中的代理设置是否正确</li>
                  <li>查看浏览器控制台是否有CORS相关错误</li>
                  <li>确认API路径是否正确（注意代理路径转换）</li>
                </ul>
              </el-collapse-item>
              
              <el-collapse-item title="2. 身份验证问题" name="2">
                <ul class="troubleshooting-list">
                  <li>检查是否已登录并获取到token</li>
                  <li>确认token是否正确添加到请求头中</li>
                  <li>检查token是否已过期</li>
                  <li>尝试重新登录获取新的token</li>
                </ul>
              </el-collapse-item>
              
              <el-collapse-item title="3. 数据格式问题" name="3">
                <ul class="troubleshooting-list">
                  <li>检查请求参数格式是否正确</li>
                  <li>确认表单数据是否按后端要求格式化</li>
                  <li>对于文件上传，检查Content-Type是否正确设置</li>
                  <li>检查日期格式是否符合后端要求</li>
                </ul>
              </el-collapse-item>
              
              <el-collapse-item title="4. 开发环境与生产环境差异" name="4">
                <ul class="troubleshooting-list">
                  <li>确认是否正确设置了环境变量</li>
                  <li>检查.env文件中的配置是否正确</li>
                  <li>确认生产环境中的API路径是否与开发环境不同</li>
                  <li>检查是否存在只在特定环境中启用的功能（如mock数据）</li>
                </ul>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ApiConnectionTest from '@/components/ApiConnectionTest.vue'

const router = useRouter()
const currentEnv = ref(import.meta.env.MODE || '未知')
const baseApiUrl = ref(import.meta.env.VITE_BASE_API || '未设置')
const browserInfo = ref(navigator.userAgent)
const currentPage = ref(window.location.href)

function goBack() {
  router.back()
}

onMounted(() => {
  console.log('API诊断页面已加载')
})
</script>

<style scoped>
.api-diagnostic-page {
  padding: 20px;
}

.page-content {
  margin-top: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.tool-description {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.troubleshooting-list {
  padding-left: 20px;
  line-height: 1.8;
}

.troubleshooting-list li {
  margin-bottom: 10px;
}
</style> 