// stores/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import service from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  
  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!user.value)

  // 登录
  const login = async (username, password) => {
    const response = await service.post('/api/user/login', null, {
      params: { username, password }
    })
    
    if (response.data.success) {
      user.value = response.data.user
      return true
    } else {
      throw new Error(response.data.message || '登录失败')
    }
  }

  // 注册
  const register = async (username, password) => {
    const response = await service.post('/api/user/register', null, {
      params: { username, password }
    })
    
    if (response.data.success) {
      return true
    } else {
      throw new Error(response.data.message || '注册失败')
    }
  }

  // 检查认证状态
  const checkAuth = async () => {
    try {
      const response = await service.get('/api/user/current', {
        hideLoading: true
      })
      
      if (response.data.success) {
        user.value = response.data.user
        return true
      } else {
        logout()
        return false
      }
    } catch (error) {
      logout()
      return false
    }
  }

  // 登出
  const logout = () => {
    user.value = null
  }

  return {
    user,
    isLoggedIn,
    login,
    register,
    checkAuth,
    logout
  }
})