// utils/request.js
import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  timeout: 10000,
  withCredentials: true
})

let loadingInstance = null

// 请求拦截器 - 添加JWT令牌
service.interceptors.request.use(
  (config) => {
    // 显示loading
    if (!config.hideLoading) {
      loadingInstance = ElLoading.service({
        text: '请求中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    
    // 从sessionStorage获取token并添加到请求头
    const token = sessionStorage.getItem('jwt-token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    if (loadingInstance) loadingInstance.close()
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理认证错误
service.interceptors.response.use(
  (response) => {
    // 关闭loading
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
    return response
  },
  (error) => {
    // 关闭loading
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
    
    const { response } = error
    
    if (response) {
      // 处理401/403错误 - 区分登录页面和其他页面
      if (response.status === 401 || response.status === 403) {
        // 检查当前是否在登录页面
        const currentPath = router.currentRoute.value.path
        if (currentPath === '/login') {
          // 在登录页面，显示具体的认证错误信息
          if (response.data?.message) {
            ElMessage.error(response.data.message)
          } else {
            ElMessage.error('用户名或密码错误')
          }
        } else {
          // 在其他页面，显示登录过期信息
          ElMessage.error('登录已过期，请重新登录')
          sessionStorage.removeItem('jwt-token')
          router.push('/login')
        }
      } else if (response.status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (response.status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service