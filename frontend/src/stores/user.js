import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  // 安全地解析localStorage中的值
  const getSafeStorageItem = (key, defaultValue = {}) => {
    try {
      const item = localStorage.getItem(key);
      if (!item || item === 'undefined') return defaultValue;
      return JSON.parse(item);
    } catch (e) {
      console.warn(`Error parsing ${key} from localStorage:`, e);
      return defaultValue;
    }
  };

  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(getSafeStorageItem('userInfo'))
  const isLoggedIn = ref(!!token.value)
  const isAdmin = ref(userInfo.value.role === 'ROLE_ROLE_ADMIN')
  console.log('userInfo-----',userInfo.value)

  console.log('isAdmin-----',isAdmin)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
    isLoggedIn.value = true
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    isAdmin.value = info.role === 'admin'
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    isLoggedIn.value = false
    isAdmin.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  async function login(username, password) {
    try {
      // 修改API路径，移除重复的/api前缀
      const response = await request.post('/auth/login', { username, password })
      console.log('Login response:', response);
      
      // 处理不同的响应格式
      // 情况1: 直接返回token等数据
      // 情况2: 包含在data字段中
      // 情况3: 包含在success和data中
      
      let tokenData = response;
      
      // 处理嵌套的返回结构
      if (response.data && typeof response.data === 'object') {
        console.log('Using nested data structure');
        tokenData = response.data;
      }
      
      // 检查返回数据中是否包含token
      if (!tokenData.token) {
        console.error('登录响应中不包含token:', response);
        
        // 尝试从其他结构中获取token
        if (tokenData.success === false) {
          console.error('登录失败，服务器返回错误:', tokenData.message || '未知错误');
          return false;
        }
        
        return false;
      }
      
      console.log('获取到token:', tokenData.token.substring(0, 10) + '...');
      setToken(tokenData.token);

      // 构建用户信息对象
      const userInfoData = {
        id: tokenData.userId || 0,
        username: tokenData.username || username,
        role: tokenData.roles && tokenData.roles.length > 0 ? 
              tokenData.roles[0].authority || tokenData.roles[0] : 'user'
      };

      console.log('用户信息数据:', userInfoData);
      setUserInfo(userInfoData)
      return true
    } catch (error) {
      console.error('登录失败:', error)
      if (error.message) {
        console.error('错误消息:', error.message);
      }
      return false
    }
  }

  async function register(userData) {
    try {
      const response = await request.post('/users/register', userData)
      return response
    } catch (error) {
      console.error('Registration failed:', error)
      throw error
    }
  }

  // 添加fetchUserInfo方法，用于路由守卫中调用
  async function fetchUserInfo() {
    try {
      const response = await request.get('/auth/user-info')
      if (response && response.code === 200 && response.data) {
        setUserInfo(response.data)
      } else {
        throw new Error('获取用户信息失败')
      }
      return userInfo.value
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      throw error
    }
  }

  async function fetchUserProfile() {
    try {
      const response = await request.get('/users/profile')
      setUserInfo(response)
      return userInfo.value
    } catch (error) {
      console.error('Failed to fetch user profile:', error)
      throw error
    }
  }

  async function updateProfile(userData) {
    try {
      const response = await request.put(`/users/${userInfo.value.id}`, userData)
      setUserInfo(response)
      return userInfo.value
    } catch (error) {
      console.error('Failed to update profile:', error)
      throw error
    }
  }

  async function changePassword(oldPassword, newPassword) {
    try {
      await request.put(`/users/${userInfo.value.id}/password`, {
        oldPassword,
        newPassword
      })
      return true
    } catch (error) {
      console.error('Failed to change password:', error)
      throw error
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    setToken,
    setUserInfo,
    logout,
    login,
    register,
    fetchUserInfo,
    fetchUserProfile,
    updateProfile,
    changePassword
  }
})