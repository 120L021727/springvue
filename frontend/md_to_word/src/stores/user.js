// stores/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import service from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  
  // 登录方法
  const login = async (username, password) => {
    try {
      const response = await service.post('/api/user/login', null, {
        params: { username, password }
      })
      
      if (response.data.success) {
        // 保存用户信息
        user.value = response.data.user
        
        // 保存JWT令牌
        sessionStorage.setItem('jwt-token', response.data.token)
        
        return true
      } else {
        throw new Error(response.data.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }
  
  // 注册方法
  const register = async (username, password) => {
    try {
      const response = await service.post('/api/user/register', null, {
        params: { username, password }
      })
      
      if (response.data.success) {
        return true
      } else {
        throw new Error(response.data.message || '注册失败')
      }
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }
  
  // 登出方法
  const logout = () => {
    user.value = null
    sessionStorage.removeItem('jwt-token')
  }
  
  // 检查认证状态
  const checkAuth = async () => {
    try {
      // 如果没有token，直接返回未认证
      if (!sessionStorage.getItem('jwt-token')) {
        return false
      }
      
      // 验证token有效性
      const response = await service.get('/api/user/current', {
        hideLoading: true
      })
      
      if (response.data.success) {
        user.value = response.data.user
        return true
      }
      
      return false
    } catch (error) {
      // 任何错误都视为未认证
      return false
    }
  }
  
  return {
    user,
    login,
    register,
    logout,
    checkAuth
  }
})