/**
 * 聊天相关的API接口
 * 提供获取聊天记录、在线用户等HTTP接口
 */

import request from './request'

/**
 * 获取公共房间聊天记录
 * @param {number} limit - 限制数量，默认50
 * @returns {Promise} 聊天记录列表
 */
export const getPublicMessages = (limit = 50) => {
  return request({
    url: '/api/chat/public/messages',
    method: 'GET',
    params: { limit }
  })
}

/**
 * 获取与指定用户的私聊记录
 * @param {number} userId - 对方用户ID
 * @param {number} limit - 限制数量，默认50
 * @returns {Promise} 私聊记录列表
 */
export const getPrivateMessages = (userId, limit = 50) => {
  return request({
    url: `/api/chat/private/messages/${userId}`,
    method: 'GET',
    params: { limit }
  })
}

/**
 * 获取在线用户列表
 * @returns {Promise} 在线用户列表
 */
export const getOnlineUsers = () => {
  return request({
    url: '/api/chat/online-users',
    method: 'GET'
  })
}

/**
 * 检查指定用户是否在线
 * @param {number} userId - 用户ID
 * @returns {Promise} 在线状态
 */
export const checkUserOnline = (userId) => {
  return request({
    url: `/api/chat/user/${userId}/online`,
    method: 'GET'
  })
}

/**
 * 清理过期用户（管理接口）
 * @returns {Promise} 清理结果
 */
export const cleanExpiredUsers = () => {
  return request({
    url: '/api/chat/clean-expired',
    method: 'POST'
  })
}
