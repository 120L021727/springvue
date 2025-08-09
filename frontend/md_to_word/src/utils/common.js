/**
 * 通用工具函数模块
 * 提供项目中常用的工具函数，包括格式化、验证、URL操作等
 */

/**
 * 格式化文件大小
 * 将字节数转换为人类可读的文件大小格式
 * @param {number} bytes 字节数
 * @param {number} decimals 小数位数，默认2位
 * @returns {string} 格式化后的文件大小，如 "1.5 MB"
 */
export function formatFileSize(bytes, decimals = 2) {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const dm = decimals < 0 ? 0 : decimals
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
}

/**
 * 防抖函数
 * 延迟执行函数，如果在延迟期间再次调用，则重新计时
 * 常用于搜索输入、窗口调整等场景
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 节流函数
 * 限制函数在一定时间内只能执行一次
 * 常用于滚动事件、按钮点击等场景
 * @param {Function} func 要节流的函数
 * @param {number} limit 限制时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit) {
  let inThrottle
  return function() {
    const args = arguments
    const context = this
    if (!inThrottle) {
      func.apply(context, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 深拷贝对象
 * 递归复制对象的所有层级，避免引用问题
 * 支持基本类型、数组、日期对象等
 * @param {any} obj 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

/**
 * 生成唯一ID
 * 基于时间戳和随机数生成唯一标识符
 * @returns {string} 唯一ID字符串
 */
export function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 验证邮箱格式
 * 使用正则表达式验证邮箱地址格式是否正确
 * @param {string} email 邮箱地址
 * @returns {boolean} 是否有效
 */
export function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证手机号格式
 * 验证中国大陆手机号格式（11位数字，以1开头）
 * @param {string} phone 手机号
 * @returns {boolean} 是否有效
 */
export function isValidPhone(phone) {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 获取URL参数
 * 从当前页面URL的查询字符串中获取指定参数的值
 * @param {string} name 参数名
 * @returns {string|null} 参数值，如果不存在返回null
 */
export function getUrlParam(name) {
  const urlParams = new URLSearchParams(window.location.search)
  return urlParams.get(name)
}

/**
 * 设置URL参数
 * 在当前页面URL中添加或更新查询参数，不刷新页面
 * @param {string} name 参数名
 * @param {string} value 参数值
 */
export function setUrlParam(name, value) {
  const url = new URL(window.location)
  url.searchParams.set(name, value)
  window.history.replaceState({}, '', url)
}

/**
 * 移除URL参数
 * 从当前页面URL的查询字符串中移除指定参数，不刷新页面
 * @param {string} name 参数名
 */
export function removeUrlParam(name) {
  const url = new URL(window.location)
  url.searchParams.delete(name)
  window.history.replaceState({}, '', url)
} 

/**
 * 将相对资源路径转换为完整可访问URL
 * - 已包含协议和主机名的路径将原样返回
 * - 相对路径会自动拼接后端基础地址（默认 http://localhost:8080）
 * @param {string} path 相对或绝对路径
 * @param {string} base 基础地址，可覆盖默认
 * @returns {string} 完整URL
 */
export function getFullUrl(path, base = 'http://localhost:8080') {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${base}${path}`
}

/**
 * 获取用户显示名：优先昵称，其次用户名
 * @param {{nickname?: string, username?: string}|null} user 用户对象
 * @returns {string} 显示名
 */
export function getDisplayName(user) {
  if (!user) return '用户'
  return user.nickname || user.username || '用户'
}