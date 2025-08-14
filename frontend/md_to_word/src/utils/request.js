/**
 * HTTP请求工具模块
 * 基于axios封装的HTTP客户端，提供统一的请求/响应拦截和错误处理
 */

import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

// 创建axios实例，配置基础URL和超时时间
const service = axios.create({
  baseURL: 'http://localhost:8080', // 后端API基础地址
  timeout: 10000 // 请求超时时间（毫秒）
})

/**
 * 请求拦截器
 * 在请求发送前统一处理：添加认证token、显示加载动画等
 */
service.interceptors.request.use(
  (config) => {
    // 智能Loading策略：
    // 1. GET请求默认不显示全屏Loading（避免页面锁定）
    // 2. POST/PUT/PATCH/DELETE等写操作默认显示Loading
    // 3. 可通过 showLoading: true 强制显示，hideLoading: true 强制隐藏
    const method = (config.method || 'get').toLowerCase()
    const isWriteOperation = ['post', 'put', 'patch', 'delete'].includes(method)
    
    let shouldShowLoading = false
    if (config.showLoading === true) {
      // 显式要求显示Loading
      shouldShowLoading = true
    } else if (config.hideLoading === true) {
      // 显式要求隐藏Loading
      shouldShowLoading = false
    } else {
      // 默认策略：写操作显示Loading，读操作不显示
      shouldShowLoading = isWriteOperation
    }
    
    if (shouldShowLoading) {
      config.loadingInstance = ElLoading.service({
        lock: true,
        text: '处理中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    
    // 从sessionStorage获取JWT Token并添加到请求头
    const token = sessionStorage.getItem('jwt-token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    // 请求配置错误，直接拒绝Promise
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在响应返回后统一处理：关闭加载动画、错误处理等
 */
service.interceptors.response.use(
  (response) => {
    // 成功响应：关闭加载动画
    if (response.config.loadingInstance) {
      response.config.loadingInstance.close()
    }
    
    return response
  },
  (error) => {
    // 错误响应：关闭加载动画
    if (error.config?.loadingInstance) {
      error.config.loadingInstance.close()
    }
    
    const { response } = error
    
    if (response) {
      // 服务器返回了错误状态码，根据状态码进行不同处理
      switch (response.status) {
        case 401:
        case 403:
          // 认证失败（未授权/禁止访问）
          const currentPath = router.currentRoute.value.path
          if (currentPath === '/login') {
            // 在登录页面：显示具体的认证错误信息
            if (response.data?.message) {
              ElMessage.error(response.data.message)
            } else {
              ElMessage.error('用户名或密码错误')
            }
          } else {
            // 在其他页面：清除无效token，让路由守卫处理跳转
            sessionStorage.removeItem('jwt-token')
            // 路由守卫会自动处理跳转和提示信息
          }
          break
          
        case 404:
          ElMessage.error('请求的资源不存在')
          break
          
        case 500:
          ElMessage.error('服务器内部错误')
          break
          
        default:
          // 其他错误状态码：显示服务器返回的错误信息或默认错误信息
          if (response.data?.message) {
            ElMessage.error(response.data.message)
          } else {
            ElMessage.error(`请求失败 (${response.status})`)
          }
      }
    } else {
      // 网络错误或其他错误（如请求超时）
      ElMessage.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)

export default service