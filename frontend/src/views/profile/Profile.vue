<template>
  <div class="profile-container">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>
    
    <el-row :gutter="20">
      <!-- 左侧用户信息卡片 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card class="user-card" shadow="hover">
          <div class="user-avatar-container">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" />
            <div class="upload-avatar">
              <el-upload
                class="avatar-uploader"
                action="/api/user/avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-icon class="upload-icon"><Edit /></el-icon>
              </el-upload>
            </div>
          </div>
          <h3 class="user-name">{{ userInfo.username }}</h3>
          <div class="user-role">{{ formatRole(userInfo.role) }}</div>
          
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.uploadCount }}</div>
              <div class="stat-label">上传档案</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.borrowCount }}</div>
              <div class="stat-label">借阅档案</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.favoriteCount }}</div>
              <div class="stat-label">收藏档案</div>
            </div>
          </div>
          
          <div class="last-login">
            <div class="last-login-label">上次登录时间</div>
            <div class="last-login-value">{{ formatDate(userInfo.lastLoginTime) }}</div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧设置标签页 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card shadow="hover">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="个人信息" name="info">
              <el-form
                :model="userForm"
                :rules="userRules"
                ref="userFormRef"
                label-width="100px"
                class="user-form"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="true" />
                </el-form-item>
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="userForm.name" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" type="email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="userForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="部门" prop="department">
                  <el-input v-model="userForm.department" placeholder="请输入部门" />
                </el-form-item>
                <el-form-item label="职位" prop="position">
                  <el-input v-model="userForm.position" placeholder="请输入职位" />
                </el-form-item>
                <el-form-item label="个人简介" prop="bio">
                  <el-input
                    v-model="userForm.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入个人简介"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateUserInfo" :loading="submitting">保存修改</el-button>
                  <el-button @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="安全设置" name="security">
              <el-form
                :model="passwordForm"
                :rules="passwordRules"
                ref="passwordFormRef"
                label-width="100px"
                class="password-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入旧密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="submitting">修改密码</el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
              
              <el-divider>安全验证</el-divider>
              
              <div class="security-item">
                <div class="security-label">
                  <div class="security-title">两步验证</div>
                  <div class="security-desc">启用两步验证可增强账户安全性</div>
                </div>
                <div class="security-action">
                  <el-switch
                    v-model="twoFactorEnabled"
                    @change="handleTwoFactorChange"
                  />
                </div>
              </div>
              
              <div class="security-item">
                <div class="security-label">
                  <div class="security-title">登录通知</div>
                  <div class="security-desc">当账户在新设备登录时接收通知</div>
                </div>
                <div class="security-action">
                  <el-switch
                    v-model="loginNotifyEnabled"
                    @change="handleLoginNotifyChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="通知设置" name="notification">
              <div class="notification-item" v-for="item in notificationSettings" :key="item.type">
                <div class="notification-label">
                  <div class="notification-title">{{ item.title }}</div>
                  <div class="notification-desc">{{ item.description }}</div>
                </div>
                <div class="notification-actions">
                  <el-checkbox v-model="item.email">邮件</el-checkbox>
                  <el-checkbox v-model="item.sms">短信</el-checkbox>
                  <el-checkbox v-model="item.system">系统</el-checkbox>
                </div>
              </div>
              
              <div class="save-notification">
                <el-button type="primary" @click="saveNotificationSettings" :loading="submitting">保存设置</el-button>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="活动记录" name="activity">
              <el-timeline>
                <el-timeline-item
                  v-for="(activity, index) in activityList"
                  :key="index"
                  :timestamp="formatDate(activity.time)"
                  :type="getActivityType(activity.action)"
                  :color="getActivityColor(activity.action)"
                >
                  {{ activity.content }}
                  <div class="activity-ip">IP: {{ activity.ip }}</div>
                </el-timeline-item>
              </el-timeline>
              
              <div class="load-more" v-if="hasMoreActivities">
                <el-button type="primary" text @click="loadMoreActivities" :loading="loadingMore">
                  加载更多
                </el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Edit } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('info')
const submitting = ref(false)
const loadingMore = ref(false)
const hasMoreActivities = ref(true)
const twoFactorEnabled = ref(false)
const loginNotifyEnabled = ref(true)

// 表单引用
const userFormRef = ref(null)
const passwordFormRef = ref(null)

// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCIgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiBmaWxsPSIjMDA3NEQ5Ij48cGF0aCBkPSJNMjQgOGM0LjQgMCA4IDMuNiA4IDhzLTMuNiA4LTggOC04LTMuNi04LTggMy42LTggOC04em0wIDIwYzguOCAwIDE2IDQgMTYgOHYzSDb2LTNjMC00IDcuMi04IDE2LTh6Ii8+PC9zdmc+'

// 用户信息
const userInfo = reactive({
  id: 1,
  username: 'admin',
  name: '管理员',
  role: 'ADMIN',
  email: 'admin@example.com',
  phone: '13800138000',
  avatar: '',
  department: '技术部',
  position: '系统管理员',
  bio: '系统管理员，负责系统的维护和管理工作。',
  lastLoginTime: '2023-08-15T08:30:00',
  uploadCount: 45,
  borrowCount: 23,
  favoriteCount: 12
})

// 用户表单
const userForm = reactive({
  username: userInfo.username,
  name: userInfo.name,
  email: userInfo.email,
  phone: userInfo.phone,
  department: userInfo.department,
  position: userInfo.position,
  bio: userInfo.bio
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 通知设置
const notificationSettings = reactive([
  {
    type: 'archive_borrow',
    title: '档案借阅',
    description: '当有档案借阅申请或状态变更时通知我',
    email: true,
    sms: false,
    system: true
  },
  {
    type: 'archive_upload',
    title: '档案上传',
    description: '当有新档案上传时通知我',
    email: false,
    sms: false,
    system: true
  },
  {
    type: 'archive_review',
    title: '档案审核',
    description: '当我上传的档案被审核时通知我',
    email: true,
    sms: true,
    system: true
  },
  {
    type: 'system_notice',
    title: '系统通知',
    description: '接收系统更新、维护等通知',
    email: true,
    sms: false,
    system: true
  }
])

// 活动记录
const activityList = ref([
  {
    action: 'login',
    content: '登录系统',
    time: '2023-08-15T08:30:00',
    ip: '192.168.1.1'
  },
  {
    action: 'upload',
    content: '上传档案"2023年第三季度财务报表"',
    time: '2023-08-15T09:45:00',
    ip: '192.168.1.1'
  },
  {
    action: 'borrow',
    content: '借阅档案"产品开发计划书"',
    time: '2023-08-14T14:20:00',
    ip: '192.168.1.1'
  },
  {
    action: 'update',
    content: '更新个人资料',
    time: '2023-08-13T16:30:00',
    ip: '192.168.1.2'
  },
  {
    action: 'return',
    content: '归还档案"市场调研报告2023"',
    time: '2023-08-12T11:15:00',
    ip: '192.168.1.1'
  }
])

// 表单验证规则
const userRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isJpgOrPng) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  }
  
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  
  return isJpgOrPng && isLt2M
}

// 头像上传成功回调
const handleAvatarSuccess = (response) => {
  if (response.code === 0) {
    userInfo.avatar = response.data.url
    ElMessage.success('头像上传成功')
    // 更新用户信息
    userStore.updateUserInfo({ avatar: response.data.url })
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 更新用户信息
const updateUserInfo = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 模拟API请求
      setTimeout(() => {
        // 更新本地状态
        Object.assign(userInfo, userForm)
        
        // 更新store中的用户信息
        userStore.updateUserInfo({
          name: userForm.name,
          email: userForm.email,
          phone: userForm.phone
        })
        
        ElMessage.success('个人信息更新成功')
        submitting.value = false
      }, 1000)
    } catch (error) {
      console.error('更新个人信息失败:', error)
      ElMessage.error('更新个人信息失败，请稍后重试')
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
    // 恢复原始数据
    Object.assign(userForm, {
      username: userInfo.username,
      name: userInfo.name,
      email: userInfo.email,
      phone: userInfo.phone,
      department: userInfo.department,
      position: userInfo.position,
      bio: userInfo.bio
    })
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 模拟API请求
      setTimeout(() => {
        ElMessage.success('密码修改成功，请重新登录')
        resetPasswordForm()
        submitting.value = false
      }, 1000)
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('修改密码失败，请稍后重试')
      submitting.value = false
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  }
}

// 处理两步验证变更
const handleTwoFactorChange = (value) => {
  const status = value ? '开启' : '关闭'
  
  ElMessage.success(`两步验证已${status}`)
}

// 处理登录通知变更
const handleLoginNotifyChange = (value) => {
  const status = value ? '开启' : '关闭'
  
  ElMessage.success(`登录通知已${status}`)
}

// 保存通知设置
const saveNotificationSettings = () => {
  submitting.value = true
  
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('通知设置已保存')
    submitting.value = false
  }, 1000)
}

// 加载更多活动
const loadMoreActivities = () => {
  loadingMore.value = true
  
  // 模拟加载更多数据
  setTimeout(() => {
    const moreActivities = [
      {
        action: 'login',
        content: '登录系统',
        time: '2023-08-10T09:10:00',
        ip: '192.168.1.1'
      },
      {
        action: 'borrow',
        content: '借阅档案"员工手册V3.0"',
        time: '2023-08-09T15:30:00',
        ip: '192.168.1.1'
      },
      {
        action: 'upload',
        content: '上传档案"会议纪要2023-08"',
        time: '2023-08-08T14:15:00',
        ip: '192.168.1.1'
      }
    ]
    
    activityList.value = [...activityList.value, ...moreActivities]
    
    // 模拟没有更多数据了
    if (activityList.value.length > 15) {
      hasMoreActivities.value = false
    }
    
    loadingMore.value = false
  }, 1000)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 格式化角色
const formatRole = (role) => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'MANAGER': '部门经理',
    'USER': '普通用户'
  }
  return roleMap[role] || role
}

// 获取活动类型
const getActivityType = (action) => {
  const typeMap = {
    'login': 'primary',
    'upload': 'success',
    'borrow': 'warning',
    'update': 'info',
    'return': 'success'
  }
  return typeMap[action] || 'info'
}

// 获取活动颜色
const getActivityColor = (action) => {
  const colorMap = {
    'login': '#409EFF',
    'upload': '#67C23A',
    'borrow': '#E6A23C',
    'update': '#909399',
    'return': '#67C23A'
  }
  return colorMap[action] || '#909399'
}

// 获取用户信息
const fetchUserInfo = () => {
  // 模拟从用户存储获取信息
  const storeUserInfo = userStore.userInfo
  
  if (storeUserInfo) {
    // 更新本地状态
    Object.assign(userInfo, storeUserInfo)
    
    // 更新表单数据
    Object.assign(userForm, {
      username: storeUserInfo.username,
      name: storeUserInfo.name,
      email: storeUserInfo.email,
      phone: storeUserInfo.phone,
      department: storeUserInfo.department,
      position: storeUserInfo.position,
      bio: storeUserInfo.bio
    })
  }
}

// 组件挂载
onMounted(() => {
  fetchUserInfo()
  
  // 从本地存储中获取设置
  const savedTwoFactor = localStorage.getItem('twoFactorEnabled')
  if (savedTwoFactor !== null) {
    twoFactorEnabled.value = savedTwoFactor === 'true'
  }
  
  const savedLoginNotify = localStorage.getItem('loginNotifyEnabled')
  if (savedLoginNotify !== null) {
    loginNotifyEnabled.value = savedLoginNotify === 'true'
  }
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.user-card {
  text-align: center;
  padding: 20px 0;
}

.user-avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.upload-avatar {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background-color: #409EFF;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-avatar:hover {
  transform: scale(1.1);
  background-color: #66b1ff;
}

.upload-icon {
  font-size: 12px;
}

.user-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 500;
}

.user-role {
  color: #909399;
  margin-bottom: 20px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.last-login {
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.last-login-label {
  margin-bottom: 4px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.security-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.security-desc {
  font-size: 12px;
  color: #909399;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notification-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  display: flex;
  gap: 16px;
}

.save-notification {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.activity-ip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.load-more {
  text-align: center;
  margin: 20px 0;
}
</style> 