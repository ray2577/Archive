<template>
  <div class="api-connection-test">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>API连接诊断</h3>
          <el-button type="primary" @click="runDiagnostics" :loading="loading">
            {{ loading ? '诊断中...' : '运行诊断' }}
          </el-button>
        </div>
      </template>
      
      <div v-if="!results" class="empty-state">
        <el-empty description="点击运行诊断按钮开始测试API连接">
          <el-button type="primary" @click="runDiagnostics">开始诊断</el-button>
        </el-empty>
      </div>
      
      <div v-else class="results">
        <el-descriptions title="基本信息" :column="1" border>
          <el-descriptions-item label="运行时间">{{ formatTime(results.timestamp) }}</el-descriptions-item>
          <el-descriptions-item label="环境模式">{{ results.environment.nodeEnv || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="Base API">{{ results.environment.baseApi || '未设置' }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <el-collapse v-model="activeNames">
          <el-collapse-item title="Vite代理连接状态" name="viteProxy">
            <el-result
              :icon="results.viteProxy.success ? 'success' : 'error'"
              :title="results.viteProxy.success ? '代理服务器正常' : '代理服务器连接失败'"
              :sub-title="results.viteProxy.message"
            >
              <template #extra>
                <el-button type="primary" @click="retestViteProxy">重新测试</el-button>
                <el-button v-if="!results.viteProxy.success" @click="showSolution('viteProxy')">查看解决方案</el-button>
              </template>
            </el-result>
            
            <div v-if="showSolutions.viteProxy" class="solution">
              <el-alert
                title="Vite代理连接问题解决方案"
                type="info"
                description="常见的代理问题包括配置错误、后端服务未启动或端口不匹配。"
                show-icon
              />
              <div class="solution-steps">
                <h4>请尝试以下步骤：</h4>
                <ol>
                  <li>确认vite.config.js中的代理配置正确无误</li>
                  <li>确认后端服务器已启动并正常运行</li>
                  <li>检查后端端口是否与代理配置中的端口一致</li>
                  <li>尝试直接访问后端API (例如: http://localhost:8080/archive/health)</li>
                </ol>
                <h4>配置示例：</h4>
                <pre class="code-block">
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '/archive')
    }
  }
}
                </pre>
              </div>
            </div>
          </el-collapse-item>
          
          <el-collapse-item title="后端直接连接状态" name="directBackend">
            <el-result
              :icon="results.directBackend.success ? 'success' : 'error'"
              :title="results.directBackend.success ? '后端服务器正常' : '后端服务器连接失败'"
              :sub-title="results.directBackend.message"
            >
              <template #extra>
                <el-button type="primary" @click="retestDirectBackend">重新测试</el-button>
                <el-button v-if="!results.directBackend.success" @click="showSolution('directBackend')">查看解决方案</el-button>
              </template>
            </el-result>
            
            <div v-if="showSolutions.directBackend" class="solution">
              <el-alert
                title="后端连接问题解决方案"
                type="info"
                description="常见的后端连接问题包括服务未启动、网络连接问题、CORS等。"
                show-icon
              />
              <div class="solution-steps">
                <h4>请尝试以下步骤：</h4>
                <ol>
                  <li>确认后端服务确实已启动（查看控制台或日志）</li>
                  <li>检查防火墙是否阻止了连接</li>
                  <li>确认后端服务正在监听正确的端口</li>
                  <li>检查后端是否已配置CORS (跨域资源共享)</li>
                </ol>
                <h4>后端CORS配置示例 (Spring Boot)：</h4>
                <pre class="code-block">
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
                </pre>
              </div>
            </div>
          </el-collapse-item>
          
          <el-collapse-item title="API请求测试" name="apiTest">
            <el-result
              :icon="results.apiTest.success ? 'success' : 'error'"
              :title="results.apiTest.success ? 'API请求成功' : 'API请求失败'"
              :sub-title="results.apiTest.message"
            >
              <template #extra>
                <el-button type="primary" @click="retestApi">重新测试</el-button>
                <el-button v-if="!results.apiTest.success" @click="showSolution('apiTest')">查看解决方案</el-button>
              </template>
            </el-result>
            
            <div v-if="showSolutions.apiTest" class="solution">
              <el-alert
                title="API请求问题解决方案"
                type="info"
                description="API请求失败可能是由多种因素引起的，包括身份验证、请求格式或服务器问题。"
                show-icon
              />
              <div class="solution-steps">
                <h4>请尝试以下步骤：</h4>
                <ol>
                  <li>检查是否需要身份验证（Token）</li>
                  <li>确认请求参数格式正确</li>
                  <li>检查API路径是否正确</li>
                  <li>查看请求和响应头以获取更多信息</li>
                  <li>检查浏览器控制台是否有CORS错误</li>
                </ol>
                <h4>典型的解决方案：</h4>
                <pre class="code-block">
// 在utils/request.js中确保设置了正确的baseURL
const service = axios.create({
  baseURL: import.meta.env.VITE_BASE_API || '',
  timeout: 30000
})

// 确保所有API路径正确
// - 如果代理设置为 /api -> /archive
// - 则API应该调用 /archives 而不是 /archive/archives
                </pre>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
        
        <el-divider />
        
        <div class="detailed-results">
          <el-button @click="showDetails = !showDetails">
            {{ showDetails ? '隐藏详细信息' : '显示详细信息' }}
          </el-button>
          
          <div v-if="showDetails" class="json-viewer">
            <pre>{{ JSON.stringify(results, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getNetworkDiagnostics, checkViteProxy, checkDirectBackend, testApiWithRequest } from '@/utils/api-check'

const loading = ref(false)
const results = ref(null)
const showDetails = ref(false)
const activeNames = ref(['viteProxy', 'directBackend', 'apiTest'])
const showSolutions = reactive({
  viteProxy: false,
  directBackend: false,
  apiTest: false
})

// 运行全部诊断
async function runDiagnostics() {
  loading.value = true
  try {
    results.value = await getNetworkDiagnostics()
    ElMessage.success('诊断完成')
  } catch (error) {
    console.error('诊断过程中出错:', error)
    ElMessage.error('诊断过程中出错: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 单独重新测试各部分
async function retestViteProxy() {
  loading.value = true
  try {
    const result = await checkViteProxy()
    if (results.value) {
      results.value.viteProxy = result
      results.value.timestamp = new Date().toISOString()
    }
    ElMessage.info('Vite代理测试完成')
  } catch (error) {
    ElMessage.error('测试失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function retestDirectBackend() {
  loading.value = true
  try {
    const result = await checkDirectBackend()
    if (results.value) {
      results.value.directBackend = result
      results.value.timestamp = new Date().toISOString()
    }
    ElMessage.info('后端直连测试完成')
  } catch (error) {
    ElMessage.error('测试失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function retestApi() {
  loading.value = true
  try {
    const result = await testApiWithRequest('/archives')
    if (results.value) {
      results.value.apiTest = result
      results.value.timestamp = new Date().toISOString()
    }
    ElMessage.info('API测试完成')
  } catch (error) {
    ElMessage.error('测试失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 显示解决方案
function showSolution(type) {
  showSolutions[type] = !showSolutions[type]
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return '未知'
  try {
    const date = new Date(timestamp)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return timestamp
  }
}
</script>

<style scoped>
.api-connection-test {
  margin: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 30px 0;
  text-align: center;
}

.solution {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.solution-steps {
  margin-top: 15px;
}

.code-block {
  background-color: #282c34;
  color: #abb2bf;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: monospace;
  margin-top: 10px;
}

.detailed-results {
  margin-top: 20px;
  text-align: center;
}

.json-viewer {
  margin-top: 15px;
  text-align: left;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow: auto;
}

.json-viewer pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style> 