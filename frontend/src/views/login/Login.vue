<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <img src="@/assets/logo.svg" alt="档案管理系统" class="logo" />
        <h1>档案管理系统</h1>
      </div>
      
      <el-form 
        ref="formRef" 
        :model="loginForm" 
        :rules="loginRules" 
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            prefix-icon="User" 
            placeholder="请输入用户名"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            prefix-icon="Lock" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-button type="link" class="forgot-password">忘记密码？</el-button>
        </div>
        
        <el-form-item class="login-submit">
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleLogin" 
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2025 档案管理系统 - 保留所有权利</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单校验规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在3到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6到20个字符之间', trigger: 'blur' }
  ]
}

const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

// 处理登录
const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 调用登录接口
      const success = await userStore.login(
        loginForm.username,
        loginForm.password
        
      )
      
      if (success) {
        // 登录成功
        ElMessage.success('登录成功')
        
        // 如果记住我，存储用户名
        if (rememberMe.value) {
          localStorage.setItem('rememberedUsername', loginForm.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }
        
        // 重定向到登录前页面或首页
        const redirect = route.query.redirect || '/dashboard'
        router.push(redirect)
      } else {
        ElMessage.error('登录失败，请检查用户名和密码')
      }
    } catch (error) {
      console.error('登录错误:', error)
      ElMessage.error(error.message || '登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 初始化
onMounted(() => {
  // 获取记住的用户名
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    rememberMe.value = true
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 40px;
  transition: all 0.3s ease;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
}

.login-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
  font-weight: 500;
}

.login-form {
  margin-bottom: 24px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-password {
  color: var(--el-color-primary);
}

.login-submit {
  margin-bottom: 0;
}

.login-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  font-size: 12px;
  color: #999;
}

/* 响应式调整 */
@media (max-width: 576px) {
  .login-card {
    padding: 30px 20px;
  }
}
</style> 