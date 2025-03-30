<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <el-menu
      :default-active="activeIndex"
      class="sidebar"
      :collapse="isCollapse"
      background-color="#001529"
      text-color="#fff"
      active-text-color="#ffd04b"
      router
    >
      <div class="logo-container">
        <img src="@/assets/logo.svg" class="logo" alt="Logo" v-if="!isCollapse" />
        <img src="@/assets/logo-small.svg" class="logo-small" alt="Logo" v-else />
      </div>
      
      <el-menu-item index="/dashboard">
        <el-icon><Monitor /></el-icon>
        <template #title>首页</template>
      </el-menu-item>
      
      <el-sub-menu index="/archive">
        <template #title>
          <el-icon><Folder /></el-icon>
          <span>档案管理</span>
        </template>
        <el-menu-item index="/archive/list">档案列表</el-menu-item>
        <el-menu-item index="/archive/search">高级搜索</el-menu-item>
        <el-menu-item index="/archive/borrow">借阅管理</el-menu-item>
      </el-sub-menu>
    
      <el-sub-menu index="/numberrule">
        <template #title>
          <span>档案规则管理</span>
        </template>
         <el-menu-item index="/numberrule/rule">档案规则管理</el-menu-item>

      </el-sub-menu>

      <el-sub-menu index="/archivemMerge">
        <template #title>
          <span>档案合并管理</span>
        </template>
              <el-menu-item index="/archivemMerge/merge">档案合并管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="/template">
        <template #title>
          <span>档案模板管理</span>
        </template>
          <el-menu-item index="/template/model">档案模板管理</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/ai-assistant">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>AI助手</template>
      </el-menu-item>
      
      <el-menu-item index="/statistics">
        <el-icon><DataLine /></el-icon>
        <template #title>统计分析</template>
      </el-menu-item>
      
      <el-menu-item index="/profile" class="profile-link">
        <el-icon><User /></el-icon>
        <template #title>个人中心</template>
      </el-menu-item>
      
      <el-sub-menu index="/system" v-if="userStore.isAdmin">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/users">用户管理</el-menu-item>
        <el-menu-item index="/system/roles">角色权限</el-menu-item>
        <el-menu-item index="/system/logs">系统日志</el-menu-item>
      </el-sub-menu>
    </el-menu>
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部栏 -->
      <div class="header">
        <el-button 
          type="text" 
          @click="toggleSidebar" 
          class="toggle-button"
        >
          <el-icon :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </el-button>
        
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item 
              v-for="(item, index) in breadcrumbItems" 
              :key="index"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="user-info">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="32" :src="userAvatar" />
              <span class="username">{{ userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="navigateTo('/profile')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Monitor, Folder, ChatDotRound, DataLine, User, Setting,
  Fold, Expand, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'

// 用户信息
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
console.log('userStore-----',userStore)
// 侧边栏状态
const isCollapse = ref(false)
const cachedViews = ref(['Dashboard', 'Profile'])
const activeIndex = ref('/dashboard')

// 默认头像
const userAvatar = computed(() => {
  return userStore.userInfo.avatar || 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCIgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiBmaWxsPSIjMDA3NEQ5Ij48cGF0aCBkPSJNMjQgOGM0LjQgMCA4IDMuNiA4IDhzLTMuNiA4LTggOC04LTMuNi04LTggMy42LTggOC04em0wIDIwYzguOCAwIDE2IDQgMTYgOHYzSDb2LTNjMC00IDcuMi04IDE2LTh6Ii8+PC9zdmc+'
})

// 面包屑导航
const breadcrumbItems = computed(() => {
  const { path, meta } = route
  const items = [
    { title: '首页', path: '/dashboard' }
  ]
  
  if (meta && meta.title) {
    if (meta.parent) {
      items.push({
        title: meta.parent.title,
        path: meta.parent.path
      })
    }
    items.push({
      title: meta.title,
      path
    })
  }
  
  return items
})

// 监听路由变化，更新激活菜单
watch(
  () => route.path,
  (newPath) => {
    const pathParts = newPath.split('/')
    if (pathParts.length > 1) {
      // 处理子路由，确保父菜单被正确激活
      if (pathParts.length > 2) {
        activeIndex.value = `/${pathParts[1]}/${pathParts[2]}`
      } else {
        activeIndex.value = `/${pathParts[1]}`
      }
    }
  },
  { immediate: true }
)

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
  localStorage.setItem('sidebarStatus', isCollapse.value ? '1' : '0')
}

// 导航
const navigateTo = (path) => {
  router.push(path)
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用登出方法
    userStore.logout()
    
    // 导航到登录页
    router.push('/login')
  } catch {
    // 用户取消操作
  }
}

// 初始化
onMounted(() => {
  // 从本地存储恢复侧边栏状态
  const sidebarStatus = localStorage.getItem('sidebarStatus')
  isCollapse.value = sidebarStatus === '1'
  
  // 如果用户未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
})
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar {
  height: 100%;
  transition: width 0.3s;
  position: relative;
  z-index: 10;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 220px;
}

.sidebar.el-menu--collapse {
  width: 64px;
}

.logo-container {
  height: 64px;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.logo {
  height: 36px;
  max-width: 100%;
}

.logo-small {
  height: 32px;
  width: 32px;
}

.profile-link {
  position: absolute;
  bottom: 16px;
  width: calc(100% - 1px);
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.toggle-button {
  margin-right: 20px;
}

.breadcrumb {
  flex: 1;
}

.user-info {
  margin-left: 20px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 8px;
  color: #606266;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7f9;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 