package com.example.mdtoword.config;

import com.example.mdtoword.pojo.ChatMessage;
import com.example.mdtoword.pojo.OnlineUser;
import com.example.mdtoword.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

/**
 * WebSocket事件监听器
 * 
 * 功能设计：
 * 1. 监听WebSocket连接建立和断开事件
 * 2. 管理用户在线状态
 * 3. 广播用户上下线消息
 * 4. 确保连接断开时的正确清理
 * 
 * 重要说明：
 * 1. 连接建立时不立即设置在线状态，而是等用户主动发送join消息
 * 2. 连接断开时自动清理用户状态并广播系统消息
 * 3. 处理网络异常断开、页面刷新等场景
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Component
public class WebSocketEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    /**
     * 处理WebSocket连接建立事件
     * 
     * 注意：此时不设置用户在线状态，因为JWT认证在连接建立后进行
     * 实际的"上线"操作在用户发送join消息时处理
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket连接建立，会话ID: {}", sessionId);
        
        // 这里不做任何用户状态操作，等待用户主动join
        // 因为此时用户认证可能还未完成
    }
    
    /**
     * 处理WebSocket连接断开事件
     * 
     * 功能：
     * 1. 从会话中获取用户信息
     * 2. 清理用户在线状态
     * 3. 广播用户离开消息
     * 4. 更新在线用户列表
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 从会话属性中获取用户信息
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Integer userId = (Integer) sessionAttributes.get("userId");
            String username = (String) sessionAttributes.get("username");
            
            if (userId != null && username != null) {
                // 处理用户下线
                handleUserDisconnect(userId, username, sessionId);
            } else {
                logger.debug("WebSocket断开连接，但会话中无用户信息，会话ID: {}", sessionId);
            }
        } else {
            logger.debug("WebSocket断开连接，无会话属性，会话ID: {}", sessionId);
        }
    }
    
    /**
     * 处理用户断开连接的具体逻辑
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     */
    private void handleUserDisconnect(Integer userId, String username, String sessionId) {
        try {
            // 检查用户是否在线（防止重复处理）
            if (!chatService.isUserOnline(userId)) {
                logger.debug("用户已离线，跳过处理: {}", username);
                return;
            }
            
            // 设置用户下线状态
            chatService.userOffline(userId, sessionId);
            
            // 创建用户离开的系统消息
            ChatMessage leaveMessage = ChatMessage.createSystemMessage(
                username + " 离开了聊天室", ChatMessage.MessageType.LEAVE);
            
            // 广播系统消息
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
            
            // 广播更新后的在线用户列表
            List<OnlineUser> onlineUsers = chatService.getOnlineUsers();
            messagingTemplate.convertAndSend("/topic/users", onlineUsers);
            
            logger.info("用户断开连接处理完成: {} ({})", username, sessionId);
            
        } catch (Exception e) {
            logger.error("处理用户断开连接时发生错误: {} ({})", username, sessionId, e);
        }
    }
}
