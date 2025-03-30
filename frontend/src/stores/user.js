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
      const response = await request.post('/api/auth/login', { username, password })
      setToken(response.token)

      // 构建用户信息对象
      const userInfoData = {
        id: response.userId || 0,
        username: response.username || username,
        role: response.roles && response.roles.length > 0 ? response.roles[0].authority : 'user'
      };

      setUserInfo(userInfoData)
      return true
    } catch (error) {
      console.error('Login failed:', error)
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
    fetchUserProfile,
    updateProfile,
    changePassword
  }
})