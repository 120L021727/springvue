<template>
  <LayoutBase>
    <div class="main-content">
      <div class="chat-container">
        <h1 class="chat-title">在线聊天室</h1>
        <p class="chat-subtitle">Hi, {{ getUsername() }}！欢迎来到聊天室</p>
        
        <!-- 连接状态 -->
        <div class="connection-status">
          <el-alert 
            v-if="!isConnected && !isConnecting"
            title="聊天室未连接" 
            type="warning" 
            show-icon
            :closable="false"
          >
            <template #default>
              <div style="display: flex; align-items: center; gap: 10px;">
                <span>点击连接按钮加入聊天室</span>
                <el-button size="small" type="primary" @click="handleConnect">连接聊天室</el-button>
              </div>
            </template>
          </el-alert>
          
          <el-alert 
            v-else-if="isConnecting"
            title="正在连接聊天室..." 
            type="info" 
            show-icon
            :closable="false"
          />
          
          <el-alert 
            v-else
            title="聊天室已连接" 
            type="success" 
            show-icon
            :closable="false"
          >
            <template #default>
              <div style="display: flex; align-items: center; gap: 10px;">
                <span>聊天室连接正常，可以开始聊天了</span>
                <el-button size="small" type="danger" @click="handleDisconnect">断开连接</el-button>
              </div>
            </template>
          </el-alert>
        </div>
        
        <!-- 聊天主界面 -->
        <div v-if="isConnected" class="chat-main">
          <div class="chat-layout">
            <!-- 左侧：聊天区域 -->
            <div class="chat-area">
              <!-- Tab切换 -->
              <el-tabs v-model="activeTab" type="card" class="chat-tabs">
                <el-tab-pane label="公共聊天" name="public">
                  <!-- 公共聊天消息 -->
                  <div class="messages-container" ref="messagesContainer">
                    <div 
                      v-for="message in messages" 
                      :key="message.id || message.timestamp"
                      :class="['message-item', { 
                        'own-message': message.isOwn,
                        'system-message': isSystemMessage(message)
                      }]"
                    >
                      <div v-if="isSystemMessage(message)" class="system-content">
                        {{ message.content }}
                      </div>
                      <div v-else class="message-content">
                        <div class="message-header">
                          <span class="sender-name">{{ message.senderName }}</span>
                          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                        </div>
                        <div class="message-text">{{ message.content }}</div>
                      </div>
                    </div>
                    
                    <!-- 无消息提示 -->
                    <div v-if="messages.length === 0" class="no-messages">
                      <el-empty description="暂无消息，开始聊天吧！" />
                    </div>
                  </div>
                  
                  <!-- 发送消息区域 -->
                  <div class="message-input-area">
                    <el-input
                      v-model="newMessage"
                      type="textarea"
                      :rows="2"
                      placeholder="输入消息内容..."
                      maxlength="500"
                      show-word-limit
                      @keyup.ctrl.enter="sendMessage"
                    />
                    <div class="input-actions">
                      <span class="input-hint">Ctrl + Enter 发送</span>
                      <el-button 
                        type="primary" 
                        @click="sendMessage"
                        :disabled="!newMessage.trim()"
                      >
                        发送
                      </el-button>
                    </div>
                  </div>
                </el-tab-pane>
                
                <!-- 私聊标签页 -->
                <el-tab-pane 
                  v-for="chat in privateChatList" 
                  :key="chat.userId"
                  :name="`private-${chat.userId}`"
                >
                  <template #label>
                    <div class="private-tab-label">
                      {{ chat.otherUser?.nickname || chat.otherUser?.username || `用户${chat.userId}` }}
                      <el-badge v-if="chat.unread > 0" :value="chat.unread" class="tab-badge" />
                      <el-button 
                        size="small" 
                        type="text" 
                        @click.stop="closePrivateChat(chat.userId)"
                        class="close-tab"
                      >
                        <el-icon><Close /></el-icon>
                      </el-button>
                    </div>
                  </template>
                  
                  <!-- 私聊消息区域 -->
                  <div class="messages-container" ref="privateMessagesContainer">
                    <div 
                      v-for="message in chat.messages" 
                      :key="message.id || message.timestamp"
                      :class="['message-item', { 'own-message': message.isOwn }]"
                    >
                      <div class="message-content">
                        <div class="message-header">
                          <span class="sender-name">{{ message.senderName }}</span>
                          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                        </div>
                        <div class="message-text">{{ message.content }}</div>
                      </div>
                    </div>
                    
                    <div v-if="chat.messages.length === 0" class="no-messages">
                      <el-empty description="开始你们的私聊吧！" />
                    </div>
                  </div>
                  
                  <!-- 私聊发送区域 -->
                  <div class="message-input-area">
                    <el-input
                      v-model="privateMessage"
                      type="textarea"
                      :rows="2"
                      placeholder="输入私聊内容..."
                      maxlength="500"
                      show-word-limit
                      @keyup.ctrl.enter="sendPrivateMessageTo(chat.userId)"
                    />
                    <div class="input-actions">
                      <span class="input-hint">Ctrl + Enter 发送</span>
                      <el-button 
                        type="primary" 
                        @click="sendPrivateMessageTo(chat.userId)"
                        :disabled="!privateMessage.trim()"
                      >
                        发送
                      </el-button>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
            
            <!-- 右侧：在线用户 -->
            <div class="users-sidebar">
              <div class="sidebar-header">
                <h3>在线用户</h3>
                <el-badge :value="onlineUsers.length" class="user-count-badge" />
              </div>
              
              <div class="users-list">
                <div 
                  v-for="user in onlineUsers" 
                  :key="user.userId"
                  class="user-item"
                  @click="startPrivateChat(user.userId)"
                >
                  <div class="user-avatar">
                    <el-avatar :size="32" :src="user.avatar">
                      {{ (user.nickname || user.username || '用')[0] }}
                    </el-avatar>
                  </div>
                  <div class="user-info">
                    <div class="user-name">{{ user.nickname || user.username }}</div>
                    <div class="user-status">在线</div>
                  </div>
                  <div class="user-actions">
                    <el-button size="small" type="text">私聊</el-button>
                  </div>
                </div>
                
                <div v-if="onlineUsers.length === 0" class="no-users">
                  <el-empty description="暂无其他在线用户" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </LayoutBase>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'
import { useAuth } from '@/composables/useAuth'
import { useChat } from '@/composables/useChat'

const { getUsername } = useAuth()
const {
  isConnected,
  isConnecting,
  messages,
  onlineUsers,
  connect,
  disconnect,
  sendPublicMessage,
  sendPrivateMessage,
  startPrivateChat: startPrivateChatAction,
  getPrivateChatList,
  loadPublicHistory,
  loadPrivateHistory,
  clearUnreadCount
} = useChat()

// 界面状态
const activeTab = ref('public')
const newMessage = ref('')
const privateMessage = ref('')
const messagesContainer = ref(null)
const privateMessagesContainer = ref(null)

// 计算属性
const privateChatList = computed(() => getPrivateChatList())

// 方法
const handleConnect = async () => {
  try {
    await connect()
    ElMessage.success('成功连接到聊天室')
    clearUnreadCount()
    
    // 加载历史消息
    await loadPublicHistory(20)
  } catch (error) {
    console.error('连接失败:', error)
  }
}

const handleDisconnect = () => {
  disconnect()
  ElMessage.info('已断开聊天室连接')
}

const sendMessage = () => {
  if (!newMessage.value.trim()) return
  
  sendPublicMessage(newMessage.value)
  newMessage.value = ''
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom(messagesContainer.value)
  })
}

const sendPrivateMessageTo = (userId) => {
  if (!privateMessage.value.trim()) return
  
  sendPrivateMessage(userId, privateMessage.value)
  privateMessage.value = ''
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom(privateMessagesContainer.value)
  })
}

const startPrivateChat = async (userId) => {
  startPrivateChatAction(userId)
  activeTab.value = `private-${userId}`
  
  // 加载私聊历史记录
  await loadPrivateHistory(userId, 20)
}

const closePrivateChat = (userId) => {
  // 如果当前在这个私聊标签页，切换到公共聊天
  if (activeTab.value === `private-${userId}`) {
    activeTab.value = 'public'
  }
  
  // 从列表中移除（这里简化处理，实际可能需要更复杂的逻辑）
  // 可以考虑只隐藏而不删除数据
}

const formatTime = (date) => {
  if (!date) return ''
  
  const now = new Date()
  const messageDate = new Date(date)
  
  // 如果是今天的消息，只显示时间
  if (messageDate.toDateString() === now.toDateString()) {
    return messageDate.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }
  
  // 否则显示日期和时间
  return messageDate.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const isSystemMessage = (message) => {
  return ['SYSTEM', 'JOIN', 'LEAVE'].includes(message.type)
}

const scrollToBottom = (container) => {
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  nextTick(() => {
    scrollToBottom(messagesContainer.value)
  })
}, { deep: true })

// 监听私聊消息变化
watch(privateChatList, () => {
  nextTick(() => {
    scrollToBottom(privateMessagesContainer.value)
  })
}, { deep: true })

// 组件挂载时自动连接
onMounted(() => {
  handleConnect()
})

// 组件卸载时断开连接
onBeforeUnmount(() => {
  disconnect()
})
</script>

<style scoped>
.chat-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.chat-title {
  text-align: center;
  color: white;
  font-size: 2.2rem;
  margin-bottom: 8px;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

.chat-subtitle {
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
  margin-bottom: 20px;
}

.connection-status {
  margin-bottom: 20px;
}

.chat-main {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-layout {
  display: flex;
  height: 70vh;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
}

.chat-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
}

:deep(.el-tab-pane) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.private-tab-label {
  display: flex;
  align-items: center;
  gap: 5px;
}

.tab-badge {
  transform: scale(0.8);
}

.close-tab {
  margin-left: 5px;
  padding: 0 4px !important;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f9fafb;
}

.message-item {
  margin-bottom: 16px;
}

.message-item.own-message .message-content {
  margin-left: auto;
  background: #3b82f6;
  color: white;
}

.message-item.own-message .message-header .sender-name {
  color: #e5e7eb;
}

.message-item.own-message .message-header .message-time {
  color: #d1d5db;
}

.message-item.system-message {
  text-align: center;
}

.system-content {
  display: inline-block;
  padding: 8px 16px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 16px;
  font-size: 14px;
  font-style: italic;
}

.message-content {
  max-width: 70%;
  background: white;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.sender-name {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.message-time {
  font-size: 12px;
  color: #9ca3af;
}

.message-text {
  line-height: 1.5;
  word-break: break-word;
}

.no-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.message-input-area {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  background: white;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.input-hint {
  font-size: 12px;
  color: #9ca3af;
}

.users-sidebar {
  width: 280px;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #374151;
}

.user-count-badge {
  transform: scale(0.9);
}

.users-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 4px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-item:hover {
  background: #f3f4f6;
  transform: translateX(2px);
}

.user-avatar {
  margin-right: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 500;
  color: #374151;
  margin-bottom: 2px;
}

.user-status {
  font-size: 12px;
  color: #10b981;
}

.user-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.user-item:hover .user-actions {
  opacity: 1;
}

.no-users {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
    height: auto;
  }
  
  .users-sidebar {
    width: 100%;
    max-height: 200px;
  }
  
  .chat-area {
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>
