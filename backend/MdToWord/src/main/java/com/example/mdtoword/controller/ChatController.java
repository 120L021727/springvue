package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.ChatMessage;
import com.example.mdtoword.pojo.OnlineUser;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.ChatService;
import com.example.mdtoword.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket聊天控制器
 * 
 * 功能设计：
 * 1. 处理WebSocket消息路由
 * 2. 管理公共房间和私聊消息
 * 3. 用户连接状态管理
 * 4. 实时消息推送
 * 
 * 路由设计：
 * - /app/chat.sendMessage -> 发送公共消息
 * - /app/chat.sendPrivate -> 发送私聊消息
 * - /app/chat.join -> 用户加入房间
 * - /app/chat.leave -> 用户离开房间
 * 
 * 推送路径：
 * - /topic/public -> 公共房间广播
 * - /queue/private -> 私聊消息
 * - /topic/users -> 在线用户更新
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Controller
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 发送公共房间消息
     * 
     * 路由: /app/chat.sendMessage
     * 推送: /topic/public
     */
    @MessageMapping("/chat.sendMessage")
    public void sendPublicMessage(@Payload ChatMessageRequest request, Principal principal) {
        try {
            // 获取当前用户信息
            Authentication auth = (Authentication) principal;
            String username = auth.getName();
            User sender = userService.findByUserName(username);
            
            if (sender == null) {
                logger.warn("发送消息失败，用户不存在: {}", username);
                return;
            }
            
            // 保存消息到数据库
            ChatMessage savedMessage = chatService.sendPublicMessage(request.getContent(), sender.getId());
            
            // 更新用户活跃时间
            chatService.updateUserActiveTime(sender.getId());
            
            // 广播到公共房间
            messagingTemplate.convertAndSend("/topic/public", savedMessage);
            
            logger.info("公共消息发送成功: {} -> {}", username, request.getContent());
            
        } catch (Exception e) {
            logger.error("发送公共消息失败", e);
        }
    }
    
    /**
     * 发送私聊消息
     * 
     * 路由: /app/chat.sendPrivate
     * 推送: /queue/private (点对点)
     */
    @MessageMapping("/chat.sendPrivate")
    public void sendPrivateMessage(@Payload PrivateMessageRequest request, Principal principal) {
        try {
            // 获取当前用户信息
            Authentication auth = (Authentication) principal;
            String username = auth.getName();
            User sender = userService.findByUserName(username);
            
            if (sender == null) {
                logger.warn("发送私聊失败，发送者不存在: {}", username);
                return;
            }
            
            // 验证接收者是否存在
            User receiver = userService.findById(request.getReceiverId());
            if (receiver == null) {
                logger.warn("发送私聊失败，接收者不存在: {}", request.getReceiverId());
                return;
            }
            
            // 保存消息到数据库
            ChatMessage savedMessage = chatService.sendPrivateMessage(
                request.getContent(), sender.getId(), request.getReceiverId());
            
            // 更新发送者活跃时间
            chatService.updateUserActiveTime(sender.getId());
            
            // 发送给接收者
            messagingTemplate.convertAndSendToUser(
                receiver.getUsername(), "/queue/private", savedMessage);
            
            // 也发送给发送者（确认消息）
            messagingTemplate.convertAndSendToUser(
                sender.getUsername(), "/queue/private", savedMessage);
            
            logger.info("私聊消息发送成功: {} -> {} : {}", 
                       username, receiver.getUsername(), request.getContent());
            
        } catch (Exception e) {
            logger.error("发送私聊消息失败", e);
        }
    }
    
    /**
     * 用户加入聊天室
     * 
     * 路由: /app/chat.join
     * 推送: /topic/public (系统消息) + /topic/users (在线用户更新)
     */
    @MessageMapping("/chat.join")
    public void joinChat(SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        try {
            // 获取当前用户信息
            Authentication auth = (Authentication) principal;
            String username = auth.getName();
            User user = userService.findByUserName(username);
            
            if (user == null) {
                logger.warn("用户加入失败，用户不存在: {}", username);
                return;
            }
            
            // 获取WebSocket会话ID
            String sessionId = headerAccessor.getSessionId();
            
            // 设置用户在线状态
            chatService.userOnline(user.getId(), sessionId);
            
            // 在会话中存储用户ID，用于连接断开时处理
            var sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put("userId", user.getId());
                sessionAttributes.put("username", username);
            }
            
            // 创建系统消息
            ChatMessage joinMessage = ChatMessage.createSystemMessage(
                username + " 加入了聊天室", ChatMessage.MessageType.JOIN);
            
            // 广播系统消息
            messagingTemplate.convertAndSend("/topic/public", joinMessage);
            
            // 广播在线用户列表更新
            List<OnlineUser> onlineUsers = chatService.getOnlineUsers();
            messagingTemplate.convertAndSend("/topic/users", onlineUsers);
            
            logger.info("用户加入聊天室: {} ({})", username, sessionId);
            
        } catch (Exception e) {
            logger.error("用户加入聊天室失败", e);
        }
    }
    
    /**
     * 消息请求DTO - 公共消息
     */
    public static class ChatMessageRequest {
        private String content;
        
        public ChatMessageRequest() {}
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 消息请求DTO - 私聊消息
     */
    public static class PrivateMessageRequest {
        private String content;
        private Integer receiverId;
        
        public PrivateMessageRequest() {}
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Integer getReceiverId() {
            return receiverId;
        }
        
        public void setReceiverId(Integer receiverId) {
            this.receiverId = receiverId;
        }
    }
}
