package com.example.mdtoword.service;

import com.example.mdtoword.pojo.ChatMessage;
import com.example.mdtoword.pojo.OnlineUser;

import java.util.List;

/**
 * 聊天服务接口
 * 
 * 功能设计：
 * 1. 聊天消息的发送和存储
 * 2. 在线用户状态管理
 * 3. 聊天记录查询
 * 4. WebSocket连接管理
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
public interface ChatService {
    
    /**
     * 发送公共房间消息
     * 
     * @param message 消息内容
     * @param senderId 发送者ID
     * @return 保存后的消息对象
     */
    ChatMessage sendPublicMessage(String message, Integer senderId);
    
    /**
     * 发送私聊消息
     * 
     * @param message 消息内容
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @return 保存后的消息对象
     */
    ChatMessage sendPrivateMessage(String message, Integer senderId, Integer receiverId);
    
    /**
     * 获取公共房间最近的聊天记录
     * 
     * @param roomId 房间ID
     * @param limit 限制数量
     * @return 聊天记录列表
     */
    List<ChatMessage> getRecentPublicMessages(String roomId, int limit);
    
    /**
     * 获取两个用户之间的私聊记录
     * 
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param limit 限制数量
     * @return 私聊记录列表
     */
    List<ChatMessage> getPrivateMessages(Integer userId1, Integer userId2, int limit);
    
    /**
     * 用户上线
     * 
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     */
    void userOnline(Integer userId, String sessionId);
    
    /**
     * 用户下线
     * 
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     */
    void userOffline(Integer userId, String sessionId);
    
    /**
     * 获取所有在线用户
     * 
     * @return 在线用户列表
     */
    List<OnlineUser> getOnlineUsers();
    
    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Integer userId);
    
    /**
     * 更新用户活跃时间
     * 
     * @param userId 用户ID
     */
    void updateUserActiveTime(Integer userId);
    
    /**
     * 清理过期的在线用户
     */
    void cleanExpiredOnlineUsers();
}
