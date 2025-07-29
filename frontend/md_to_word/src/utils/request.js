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

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 显示loading
    if (!config.hideLoading) {
      loadingInstance = ElLoading.service({
        text: '请求中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
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
      switch (response.status) {
        case 401:
        case 403:
          ElMessage.error('登录已过期，请重新登录')
          
          // 清除Pinia状态（避免循环导入）
          if (typeof window !== 'undefined') {
            sessionStorage.removeItem('user-store')
          }
          
          router.push('/login')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service