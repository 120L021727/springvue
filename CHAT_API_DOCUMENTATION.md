# 聊天室系统 API 文档

## 🏗️ 系统架构

### 技术栈
- **WebSocket + STOMP**: 实时通信
- **JWT认证**: 复用现有安全框架
- **Redis**: 在线用户状态管理
- **MySQL**: 消息持久化存储

### 无Session设计
- 完全无状态设计，复用现有JWT认证
- 用户状态存储在Redis中，支持水平扩展
- WebSocket连接时验证JWT Token

## 🔌 WebSocket连接

### 连接端点
```
ws://localhost:8080/ws/chat
```

### 认证方式
在WebSocket连接头中添加JWT Token：
```javascript
const headers = {
    'Authorization': `Bearer ${jwtToken}`,
    // 或者使用自定义header
    'X-Auth-Token': jwtToken
};
```

### 连接示例（前端）
```javascript
const stompClient = Stomp.over(new SockJS('/ws/chat'));
stompClient.connect(headers, function(frame) {
    console.log('Connected: ' + frame);
    
    // 加入聊天室
    stompClient.send("/app/chat.join", {}, {});
    
    // 订阅公共消息
    stompClient.subscribe('/topic/public', function(message) {
        const chatMessage = JSON.parse(message.body);
        displayMessage(chatMessage);
    });
    
    // 订阅私聊消息
    stompClient.subscribe('/queue/private', function(message) {
        const chatMessage = JSON.parse(message.body);
        displayPrivateMessage(chatMessage);
    });
    
    // 订阅在线用户更新
    stompClient.subscribe('/topic/users', function(message) {
        const onlineUsers = JSON.parse(message.body);
        updateOnlineUsersList(onlineUsers);
    });
});
```

## 📡 WebSocket消息路由

### 发送消息

#### 1. 发送公共消息
```javascript
stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
    content: "Hello, everyone!"
}));
```

#### 2. 发送私聊消息
```javascript
stompClient.send("/app/chat.sendPrivate", {}, JSON.stringify({
    content: "Hello, this is a private message",
    receiverId: 123
}));
```

#### 3. 加入聊天室
```javascript
stompClient.send("/app/chat.join", {}, {});
```

### 接收消息

#### 1. 公共房间消息
**订阅**: `/topic/public`
**消息格式**:
```json
{
    "id": 1,
    "content": "Hello, everyone!",
    "type": "TEXT",
    "chatType": "PUBLIC",
    "senderId": 1,
    "senderName": "john_doe",
    "roomId": "public",
    "createTime": "2025-08-24T10:30:00"
}
```

#### 2. 私聊消息
**订阅**: `/queue/private`
**消息格式**:
```json
{
    "id": 2,
    "content": "Hello, this is private",
    "type": "TEXT",
    "chatType": "PRIVATE",
    "senderId": 1,
    "senderName": "john_doe",
    "receiverId": 2,
    "createTime": "2025-08-24T10:31:00"
}
```

#### 3. 在线用户列表
**订阅**: `/topic/users`
**消息格式**:
```json
[
    {
        "userId": 1,
        "username": "john_doe",
        "nickname": "John",
        "sessionId": "abc123",
        "status": "ONLINE",
        "lastActiveTime": "2025-08-24T10:30:00",
        "loginTime": "2025-08-24T10:00:00",
        "roomId": "public"
    }
]
```

## 🌐 REST API

### 获取聊天记录

#### 1. 获取公共房间消息
```http
GET /api/chat/public/messages?limit=50
Authorization: Bearer {jwt_token}
```

#### 2. 获取私聊记录
```http
GET /api/chat/private/messages/{userId}?limit=50
Authorization: Bearer {jwt_token}
```

#### 3. 获取在线用户
```http
GET /api/chat/online-users
Authorization: Bearer {jwt_token}
```

#### 4. 检查用户在线状态
```http
GET /api/chat/user/{userId}/online
Authorization: Bearer {jwt_token}
```

## 💾 数据库设计

### 聊天消息表 (tb_chat_message)
```sql
CREATE TABLE `tb_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `content` text NOT NULL COMMENT '消息内容',
  `type` varchar(20) NOT NULL COMMENT '消息类型：TEXT, SYSTEM, JOIN, LEAVE',
  `chat_type` varchar(20) NOT NULL COMMENT '聊天类型：PUBLIC, PRIVATE',
  `sender_id` int DEFAULT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) DEFAULT NULL COMMENT '发送者用户名',
  `receiver_id` int DEFAULT NULL COMMENT '接收者ID（私聊时使用）',
  `room_id` varchar(50) DEFAULT NULL COMMENT '房间ID（公共聊天时使用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
);
```

## 🔄 用户状态管理

### 在线状态存储（Redis）
- **Key格式**: `chat:online:{userId}`
- **Value**: OnlineUser对象的JSON字符串
- **TTL**: 5分钟自动过期

### 状态流转
1. **上线**: WebSocket连接 + 发送join消息
2. **活跃**: 发送消息时自动更新活跃时间
3. **下线**: WebSocket断开连接自动处理

### 页面刷新处理
- 页面刷新会触发WebSocket重连
- 重连时会重新发送join消息
- 系统会适当处理重复上线通知

## ⚠️ 注意事项

### 1. 连接安全
- 必须在WebSocket连接时提供有效的JWT Token
- Token过期或无效会拒绝连接
- 支持单点登录，Token被顶下线时WebSocket连接会断开

### 2. 消息可靠性
- 公共消息广播到所有在线用户
- 私聊消息只发送给指定用户
- 离线用户的消息会保存在数据库中

### 3. 性能考虑
- 在线用户状态存储在Redis中，支持高并发
- 聊天记录分页查询，避免大量数据传输
- 定时清理过期的在线用户记录

### 4. 扩展性
- 支持多房间聊天（通过roomId区分）
- 可以扩展消息类型（图片、文件等）
- 可以添加消息状态（已读、未读等）

## 🚀 部署说明

### 必需依赖
- Redis服务（用于在线状态管理）
- MySQL数据库（用于消息存储）
- 执行chat_tables.sql创建相关表

### 配置检查
- 确保Redis连接正常
- 确认WebSocket端口开放
- 验证CORS配置正确

### 前端集成
- 使用SockJS + STOMP.js库
- 在登录后建立WebSocket连接
- 在页面卸载时正确关闭连接
