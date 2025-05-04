<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapse }">
      <div class="logo-container">
        <img src="@/assets/logo.svg" class="logo" alt="Logo" v-if="!isCollapse" />
        <img src="@/assets/logo-small.svg" class="logo-small" alt="Logo" v-else />
      </div>
      
      <div class="menu-wrapper">
        <el-menu
          :default-active="activeIndex"
          class="sidebar-menu"
          :collapse="isCollapse"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
          :collapse-transition="false"
          unique-opened
        >
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <el-sub-menu index="/archive" popper-class="sidebar-submenu">
            <template #title>
              <el-icon><Folder /></el-icon>
              <span>档案管理</span>
            </template>
            <el-menu-item index="/archive/list">
              <el-icon><Document /></el-icon>档案列表
            </el-menu-item>
            <el-menu-item index="/archive/search">
              <el-icon><Search /></el-icon>高级搜索
            </el-menu-item>
            <el-menu-item index="/archive/borrow">
              <el-icon><Files /></el-icon>借阅管理
            </el-menu-item>
          </el-sub-menu>
        
          <el-sub-menu index="/numberrule" popper-class="sidebar-submenu">
            <template #title>
              <el-icon><Collection /></el-icon>
              <span>档案规则管理</span>
            </template>
             <el-menu-item index="/numberrule/rule">
              <el-icon><List /></el-icon>规则列表
             </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/archiveMerge" popper-class="sidebar-submenu">
            <template #title>
              <el-icon><DocumentCopy /></el-icon>
              <span>档案合并管理</span>
            </template>
            <el-menu-item index="/archiveMerge/merge">
              <el-icon><Histogram /></el-icon>合并列表
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/template" popper-class="sidebar-submenu">
            <template #title>
              <el-icon><Grid /></el-icon>
              <span>档案模板管理</span>
            </template>
            <el-menu-item index="/template/model">
              <el-icon><Menu /></el-icon>模板列表
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/ai-assistant">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI助手</template>
          </el-menu-item>
          
          <el-menu-item index="/statistics">
            <el-icon><DataLine /></el-icon>
            <template #title>统计分析</template>
          </el-menu-item>
          
          <el-sub-menu index="/system" v-if="userStore.isAdmin" popper-class="sidebar-submenu">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/users">
              <el-icon><UserFilled /></el-icon>用户管理
            </el-menu-item>
            <el-menu-item index="/system/roles">
              <el-icon><Lock /></el-icon>角色权限
            </el-menu-item>
            <el-menu-item index="/system/logs">
              <el-icon><Tickets /></el-icon>系统日志
            </el-menu-item>
            <el-menu-item index="/api-diagnostic">
              <el-icon><Connection /></el-icon>API诊断工具
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      
      <div class="profile-menu">
        <el-menu
          :collapse="isCollapse"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
        >
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    
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
  Fold, Expand, ArrowDown, SwitchButton,
  Document, Search, Files, Collection, List, DocumentCopy, Histogram, Grid, Menu, UserFilled, Lock, Tickets, Connection
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

.sidebar-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: width 0.3s;
  position: relative;
  z-index: 10;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 220px;
  background-color: #001529;
}

.sidebar-container.is-collapsed {
  width: 64px;
}

.logo-container {
  height: 64px;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.logo {
  height: 36px;
  max-width: 100%;
}

.logo-small {
  height: 32px;
  width: 32px;
}

.menu-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.profile-menu {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

/* 自定义滚动条 */
.menu-wrapper::-webkit-scrollbar {
  width: 4px;
}

.menu-wrapper::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.menu-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

/* 菜单样式优化 */
:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-menu-item) .el-icon, 
:deep(.el-sub-menu__title) .el-icon {
  margin-right: 10px;
  font-size: 18px;
  vertical-align: middle;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 控制子菜单弹出样式 */
:deep(.sidebar-submenu) {
  background-color: #000c17 !important;
  border: none !important;
}

:deep(.sidebar-submenu .el-menu) {
  background-color: #000c17 !important;
}

:deep(.sidebar-submenu .el-menu-item) {
  background-color: #000c17 !important;
  color: #fff !important;
}

:deep(.sidebar-submenu .el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.sidebar-submenu .el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff !important;
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