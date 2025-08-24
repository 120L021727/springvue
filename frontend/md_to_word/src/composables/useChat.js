/**
 * 聊天室WebSocket连接管理
 * 提供聊天室连接、消息收发、在线用户管理等功能
 */

import { ref, reactive, onBeforeUnmount } from 'vue'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { ElMessage } from 'element-plus'
import { useAuth } from './useAuth'
import { getPublicMessages, getPrivateMessages } from '@/utils/chatApi'

export function useChat() {
  const { getUser, isLoggedIn } = useAuth()
  
  // 连接状态
  const isConnected = ref(false)
  const isConnecting = ref(false)
  
  // WebSocket相关
  let stompClient = null
  let socket = null
  
  // 聊天数据
  const messages = ref([])
  const onlineUsers = ref([])
  const unreadCount = ref(0)
  
  // 私聊相关
  const privateChats = ref({}) // userId -> {messages: [], unread: 0}
  const currentPrivateChat = ref(null) // 当前私聊对象的userId
  
  /**
   * 连接到聊天室
   */
  const connect = () => {
    if (!isLoggedIn()) {
      ElMessage.error('请先登录')
      return Promise.reject('未登录')
    }
    
    if (isConnected.value || isConnecting.value) {
      return Promise.resolve()
    }
    
    return new Promise((resolve, reject) => {
      try {
        isConnecting.value = true
        
        // 创建WebSocket连接
        socket = new SockJS('/ws/chat')
        stompClient = Stomp.over(socket)
        
        // 禁用控制台日志
        stompClient.debug = null
        
        // 获取JWT Token
        const token = sessionStorage.getItem('jwt-token')
        
        // 连接头信息
        const headers = {
          'Authorization': `Bearer ${token}`
        }
        
        // 建立连接
        stompClient.connect(headers, 
          (frame) => {
            console.log('聊天室连接成功:', frame)
            isConnected.value = true
            isConnecting.value = false
            
            // 加入聊天室
            stompClient.send("/app/chat.join", {}, {})
            
            // 订阅公共消息
            stompClient.subscribe('/topic/public', (message) => {
              const chatMessage = JSON.parse(message.body)
              handlePublicMessage(chatMessage)
            })
            
            // 订阅私聊消息
            stompClient.subscribe('/user/queue/private', (message) => {
              const chatMessage = JSON.parse(message.body)
              handlePrivateMessage(chatMessage)
            })
            
            // 订阅在线用户列表
            stompClient.subscribe('/topic/users', (message) => {
              const users = JSON.parse(message.body)
              updateOnlineUsers(users)
            })
            
            resolve()
          },
          (error) => {
            console.error('聊天室连接失败:', error)
            isConnecting.value = false
            ElMessage.error('聊天室连接失败')
            reject(error)
          }
        )
      } catch (error) {
        isConnecting.value = false
        reject(error)
      }
    })
  }
  
  /**
   * 断开连接
   */
  const disconnect = () => {
    if (stompClient && isConnected.value) {
      stompClient.disconnect(() => {
        console.log('聊天室已断开连接')
      })
    }
    
    isConnected.value = false
    isConnecting.value = false
    stompClient = null
    socket = null
    
    // 清理数据
    messages.value = []
    onlineUsers.value = []
    privateChats.value = {}
    currentPrivateChat.value = null
    unreadCount.value = 0
  }
  
  /**
   * 发送公共消息
   */
  const sendPublicMessage = (content) => {
    if (!isConnected.value || !stompClient) {
      ElMessage.error('聊天室未连接')
      return
    }
    
    if (!content.trim()) {
      return
    }
    
    try {
      stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
        content: content.trim()
      }))
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败')
    }
  }
  
  /**
   * 发送私聊消息
   */
  const sendPrivateMessage = (receiverId, content) => {
    if (!isConnected.value || !stompClient) {
      ElMessage.error('聊天室未连接')
      return
    }
    
    if (!content.trim()) {
      return
    }
    
    try {
      stompClient.send("/app/chat.sendPrivate", {}, JSON.stringify({
        content: content.trim(),
        receiverId: receiverId
      }))
    } catch (error) {
      console.error('发送私聊失败:', error)
      ElMessage.error('发送私聊失败')
    }
  }
  
  /**
   * 处理公共消息
   */
  const handlePublicMessage = (message) => {
    messages.value.push({
      ...message,
      timestamp: new Date(message.createTime),
      isOwn: message.senderId === getUser()?.id
    })
    
    // 限制消息数量，避免内存过大
    if (messages.value.length > 100) {
      messages.value = messages.value.slice(-100)
    }
    
    // 更新未读数（如果不在聊天室页面）
    if (!isCurrentPage('/chat')) {
      unreadCount.value++
    }
  }
  
  /**
   * 处理私聊消息
   */
  const handlePrivateMessage = (message) => {
    const currentUserId = getUser()?.id
    if (!currentUserId) return
    
    // 确定对话方ID
    const otherUserId = message.senderId === currentUserId ? message.receiverId : message.senderId
    
    // 初始化私聊记录
    if (!privateChats.value[otherUserId]) {
      privateChats.value[otherUserId] = {
        messages: [],
        unread: 0,
        otherUser: null // 会在更新在线用户时设置
      }
    }
    
    // 添加消息
    const chat = privateChats.value[otherUserId]
    const processedMessage = {
      ...message,
      timestamp: new Date(message.createTime),
      isOwn: message.senderId === currentUserId
    }
    
    chat.messages.push(processedMessage)
    
    // 限制消息数量
    if (chat.messages.length > 50) {
      chat.messages = chat.messages.slice(-50)
    }
    
    // 更新未读数
    if (currentPrivateChat.value !== otherUserId && !isCurrentPage('/chat')) {
      chat.unread++
    }
  }
  
  /**
   * 更新在线用户列表
   */
  const updateOnlineUsers = (users) => {
    const currentUserId = getUser()?.id
    onlineUsers.value = users.filter(user => user.userId !== currentUserId)
    
    // 更新私聊中的用户信息
    onlineUsers.value.forEach(user => {
      if (privateChats.value[user.userId]) {
        privateChats.value[user.userId].otherUser = user
      }
    })
  }
  
  /**
   * 开始私聊
   */
  const startPrivateChat = (userId) => {
    if (!privateChats.value[userId]) {
      privateChats.value[userId] = {
        messages: [],
        unread: 0,
        otherUser: onlineUsers.value.find(u => u.userId === userId)
      }
    }
    
    currentPrivateChat.value = userId
    // 清除未读数
    privateChats.value[userId].unread = 0
  }
  
  /**
   * 获取私聊记录
   */
  const getPrivateChat = (userId) => {
    return privateChats.value[userId] || { messages: [], unread: 0, otherUser: null }
  }
  
  /**
   * 获取所有私聊列表
   */
  const getPrivateChatList = () => {
    return Object.entries(privateChats.value).map(([userId, chat]) => ({
      userId: parseInt(userId),
      ...chat
    }))
  }
  
  /**
   * 加载公共房间历史消息
   */
  const loadPublicHistory = async (limit = 20) => {
    try {
      const response = await getPublicMessages(limit)
      if (response.success && response.data) {
        messages.value = response.data.map(msg => ({
          ...msg,
          timestamp: new Date(msg.createTime),
          isOwn: msg.senderId === getUser()?.id
        }))
      }
    } catch (error) {
      console.error('加载历史消息失败:', error)
    }
  }
  
  /**
   * 加载私聊历史消息
   */
  const loadPrivateHistory = async (userId, limit = 20) => {
    try {
      const response = await getPrivateMessages(userId, limit)
      if (response.success && response.data) {
        if (!privateChats.value[userId]) {
          privateChats.value[userId] = {
            messages: [],
            unread: 0,
            otherUser: onlineUsers.value.find(u => u.userId === userId)
          }
        }
        
        const chat = privateChats.value[userId]
        chat.messages = response.data.map(msg => ({
          ...msg,
          timestamp: new Date(msg.createTime),
          isOwn: msg.senderId === getUser()?.id
        }))
      }
    } catch (error) {
      console.error('加载私聊历史失败:', error)
    }
  }
  
  /**
   * 清除未读数
   */
  const clearUnreadCount = () => {
    unreadCount.value = 0
  }
  
  /**
   * 检查是否在指定页面
   */
  const isCurrentPage = (path) => {
    return window.location.pathname === path
  }
  
  // 组件卸载时断开连接
  onBeforeUnmount(() => {
    disconnect()
  })
  
  return {
    // 状态
    isConnected,
    isConnecting,
    messages,
    onlineUsers,
    unreadCount,
    currentPrivateChat,
    
    // 方法
    connect,
    disconnect,
    sendPublicMessage,
    sendPrivateMessage,
    startPrivateChat,
    getPrivateChat,
    getPrivateChatList,
    loadPublicHistory,
    loadPrivateHistory,
    clearUnreadCount
  }
}
