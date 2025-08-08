/**
 * 用户认证相关的Composable
 * 提供统一的用户状态检查和管理功能，封装了常用的认证相关操作
 */

import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 用户认证相关的组合式函数
 * 提供用户状态管理、登录检查、用户名获取等功能
 * @returns {Object} 包含认证相关方法和状态的对象
 */
export function useAuth() {
  const userStore = useUserStore()

  /**
   * 检查并初始化用户状态
   * 当组件挂载时，如果本地有token但没有用户信息，则尝试获取用户信息
   */
  const initUserState = async () => {
    // 如果没有用户信息但有token，尝试获取用户信息
    if (!userStore.user && sessionStorage.getItem('jwt-token')) {
      await userStore.checkAuth()
    }
  }

  /**
   * 自动初始化用户状态
   * 在组件挂载时自动调用initUserState
   * 使用方式：在setup中调用 autoInitUser()
   */
  const autoInitUser = () => {
    onMounted(initUserState)
  }

  /**
   * 获取用户显示名称
   * 优先返回昵称，如果没有昵称则返回用户名，都没有则返回默认值
   * @returns {string} 用户显示名称
   */
  const getUsername = () => {
    if (userStore.user) {
      // 优先使用昵称，如果昵称为空或未设置则使用用户名
      return userStore.user.nickname || userStore.user.username || '用户'
    }
    return '用户'
  }

  /**
   * 检查是否已登录
   * 同时检查用户信息和token的存在性
   * @returns {boolean} 是否已登录
   */
  const isLoggedIn = () => {
    return !!userStore.user && !!sessionStorage.getItem('jwt-token')
  }

  /**
   * 获取当前登录用户的ID
   * @returns {number|null} 用户ID，如果未登录则返回null
   */
  const getCurrentUserId = () => {
    if (userStore.user && userStore.user.id) {
      return userStore.user.id
    }
    return null
  }

  /**
   * 获取用户信息
   * 返回完整的用户对象
   * @returns {Object|null} 用户信息对象或null
   */
  const getUser = () => {
    return userStore.user
  }

  return {
    userStore,         // Pinia用户状态管理实例
    initUserState,     // 初始化用户状态方法
    autoInitUser,      // 自动初始化方法
    getUsername,       // 获取用户名方法
    isLoggedIn,        // 登录状态检查方法
    getCurrentUserId,  // 获取当前用户ID方法
    getUser            // 获取用户信息方法
  }
} 