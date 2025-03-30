import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layouts/MainLayout.vue'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'Monitor', affix: true }
      }
    ]
  },
  {
    path: '/archive',
    component: MainLayout,
    redirect: '/archive/ArchiveList',
    meta: { title: '档案管理', icon: 'Folder' },
    children: [
      {
        path: 'list',
        name: 'ArchiveList',
        component: () => import('@/views/archive/ArchiveList.vue'),
        meta: { title: '档案列表', parent: { title: '档案管理', path: '/archive' } }
      },
      {
        path: 'search',
        name: 'ArchiveSearch',
        component: () => import('@/views/archive/ArchiveSearch.vue'),
        meta: { title: '高级搜索', parent: { title: '档案管理', path: '/archive' } }
      },
      {
        path: 'borrow',
        name: 'ArchiveBorrow',
        component: () => import('@/views/archive/ArchiveBorrow.vue'),
        meta: { title: '借阅管理', parent: { title: '档案管理', path: '/archive' } }
      },
      {
        path: 'detail/:id',
        name: 'ArchiveDetail',
        component: () => import('@/views/archive/ArchiveDetail.vue'),
        meta: { title: '档案详情', parent: { title: '档案管理', path: '/archive' }, hidden: true }
      }
    ]
  },
  {
    path: '/ai-assistant',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'AIAssistant',
        component: () => import('@/views/ai-assistant/AiAssistant.vue'),
        meta: { title: 'AI助手', icon: 'ChatDotRound' }
      }
    ]
  },
  
  {
    path: '/numberrule',
    component: MainLayout,
    children: [
      {
        path: 'rule',
        name: 'ArchiveNumberRule',
        component: () => import('@/views/numberrule/ArchiveNumberRule.vue'),
        meta: { title: '档案规则', icon: 'ChatDotRound' }
      }
    ]
  },

  {
    path: '/template',
    component: MainLayout,
    children: [
      {
        path: 'model',
        name: 'Template',
        component: () => import('@/views/template/templateView.vue'),
        meta: { title: '档案模板管理', icon: '' }
      }
    ]
  },
  {
    path: '/archivemMerge',
    component: MainLayout,
    children: [
      {
        path: 'merge',
        name: 'Merge',
        component: () => import('@/views/merge/PdfMergeView.vue'),
        meta: { title: '档案模板管理', icon: '' }
      }
    ]
  },
  {
    path: '/system',
    component: MainLayout,
    redirect: '/system/users',
    meta: { title: '系统管理', icon: 'Setting', roles: ['admin'] },
    children: [
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/users.vue'),
        meta: { title: '用户管理', parent: { title: '系统管理', path: '/system' } }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/system/roles.vue'),
        meta: { title: '角色权限', parent: { title: '系统管理', path: '/system' } }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/system/logs.vue'),
        meta: { title: '系统日志', parent: { title: '系统管理', path: '/system' } }
      }
    ]
  },
  {
    path: '/statistics',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '统计分析', icon: 'DataLine' }
      }
    ]
  },
  {
    path: '/profile',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心', icon: 'User' }
      }
    ]
  },
 
  {
    path: '/404',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true }
  },
  {
    path: '/dev',
    component: MainLayout,
    meta: { title: '开发工具', icon: 'Tools', hidden: process.env.NODE_ENV !== 'development' },
    children: [
      {
        path: 'api-debug',
        name: 'ApiDebug',
        component: () => import('@/views/dev/ApiDebug.vue'),
        meta: { title: 'API调试', icon: 'Monitor' }
      },
      {
        path: 'api-test',
        name: 'ApiTest',
        component: () => import('@/views/dev/ApiTest.vue'),
        meta: { title: 'API路径测试', icon: 'Connection' }
      },
      {
        path: 'api-docs',
        name: 'ApiDocs',
        component: () => import('@/views/dev/ApiDocs.vue'),
        meta: { title: 'API文档', icon: 'Document' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 档案管理系统` : '档案管理系统'
  
  const userStore = useUserStore()
  const { token, isLoggedIn } = userStore
  
  // 白名单路径，不需要登录
  const whiteList = ['/login', '/404']
  
  if (isLoggedIn) {
    if (to.path === '/login') {
      // 已登录，访问登录页则重定向到首页
      next({ path: '/' })
    } else {
      // 检查用户信息是否已获取
      if (!userStore.userInfo.id) {
        try {
          // 获取用户信息
          await userStore.fetchUserInfo()
          
          // 检查路由权限
          if (to.meta.roles && !to.meta.roles.some(role => userStore.userInfo.roles.includes(role))) {
            next('/404')
          } else {
            next()
          }
        } catch (error) {
          // 获取用户信息失败，清除token并跳转到登录页
          userStore.removeToken()
          next(`/login?redirect=${to.path}`)
        }
      } else {
        next()
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 白名单路径直接访问
      next()
    } else {
      // 重定向到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router