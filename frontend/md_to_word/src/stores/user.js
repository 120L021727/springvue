/**
 * 用户状态管理模块
 * 基于Pinia的用户状态管理，包括用户信息、认证状态、注册登出等功能
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import service from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  // 用户信息状态
  const user = ref(null)
  
  /**
   * 用户注册 - 重构为纯JWT认证模式
   * 
   * 重构说明：
   * 1. 更新API端点为 /api/auth/register
   * 2. 改用JSON格式发送请求数据
   * 3. 保持相同的错误处理逻辑
   * 
   * @param {string} username 用户名
   * @param {string} password 密码
   * @returns {Promise<boolean>} 注册是否成功
   * @throws {Error} 注册失败时抛出错误
   */
  const register = async (username, password) => {
    try {
      // 发送JSON格式的注册请求到新的认证端点
      const response = await service.post('/api/auth/register', {
        username,
        password
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
  
  /**
   * 用户登出
   * 清除本地用户信息和认证token
   */
  const logout = () => {
    user.value = null
    sessionStorage.removeItem('jwt-token')
    // 清除保存的跳转路径，避免登出后仍保留重定向信息
    sessionStorage.removeItem('redirectAfterLogin')
  }
  
  /**
   * 检查用户认证状态
   * 验证当前token是否有效，并更新用户信息
   * @returns {Promise<boolean>} 是否已认证
   */
  const checkAuth = async () => {
    try {
      // 如果没有token，直接返回未认证
      if (!sessionStorage.getItem('jwt-token')) {
        return false
      }
      
      // 向后端验证token有效性并获取当前用户信息
      const response = await service.get('/api/user/current', {
        hideLoading: true // 隐藏加载动画，避免频繁显示
      })
      
      if (response.data.success) {
        user.value = response.data.data
        return true
      }
      
      return false
    } catch (error) {
      // 任何错误都视为未认证（网络错误、token过期等）
      console.warn('Token验证失败:', error.message)
      return false
    }
  }
  
  return {
    user,        // 用户信息
    register,    // 注册方法
    logout,      // 登出方法
    checkAuth    // 认证检查方法
  }
})