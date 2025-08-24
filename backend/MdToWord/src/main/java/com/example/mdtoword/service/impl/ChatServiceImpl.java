package com.example.mdtoword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mdtoword.mapper.ChatMessageMapper;
import com.example.mdtoword.pojo.ChatMessage;
import com.example.mdtoword.pojo.OnlineUser;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.ChatService;
import com.example.mdtoword.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 聊天服务实现类
 * 
 * 功能实现：
 * 1. 消息的发送、存储和查询
 * 2. 基于Redis的在线用户状态管理
 * 3. WebSocket会话管理
 * 4. 自动清理过期用户
 * 
 * Redis数据结构：
 * - Key: "chat:online:{userId}" -> OnlineUser JSON
 * - TTL: 5分钟自动过期
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    // Redis Key前缀
    private static final String ONLINE_USER_PREFIX = "chat:online:";
    // 用户在线超时时间（分钟）
    private static final int ONLINE_TIMEOUT_MINUTES = 5;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public ChatMessage sendPublicMessage(String message, Integer senderId) {
        User sender = userService.findById(senderId);
        if (sender == null) {
            throw new RuntimeException("发送者不存在");
        }
        
        ChatMessage chatMessage = ChatMessage.createPublicMessage(message, senderId, sender.getUsername());
        chatMessageMapper.insert(chatMessage);
        
        logger.info("公共消息发送成功，发送者: {}, 内容: {}", sender.getUsername(), message);
        return chatMessage;
    }
    
    @Override
    public ChatMessage sendPrivateMessage(String message, Integer senderId, Integer receiverId) {
        User sender = userService.findById(senderId);
        User receiver = userService.findById(receiverId);
        
        if (sender == null || receiver == null) {
            throw new RuntimeException("发送者或接收者不存在");
        }
        
        ChatMessage chatMessage = ChatMessage.createPrivateMessage(message, senderId, sender.getUsername(), receiverId);
        chatMessageMapper.insert(chatMessage);
        
        logger.info("私聊消息发送成功，发送者: {}, 接收者: {}, 内容: {}", 
                   sender.getUsername(), receiver.getUsername(), message);
        return chatMessage;
    }
    
    @Override
    public List<ChatMessage> getRecentPublicMessages(String roomId, int limit) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getChatType, ChatMessage.ChatType.PUBLIC)
                   .eq(ChatMessage::getRoomId, roomId)
                   .orderByDesc(ChatMessage::getCreateTime)
                   .last("LIMIT " + limit);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(queryWrapper);
        // 按时间正序返回（最新的在最后）
        return messages.stream().sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime())).toList();
    }
    
    @Override
    public List<ChatMessage> getPrivateMessages(Integer userId1, Integer userId2, int limit) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getChatType, ChatMessage.ChatType.PRIVATE)
                   .and(wrapper -> wrapper
                       .and(w1 -> w1.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
                       .or(w2 -> w2.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1)))
                   .orderByDesc(ChatMessage::getCreateTime)
                   .last("LIMIT " + limit);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(queryWrapper);
        // 按时间正序返回（最新的在最后）
        return messages.stream().sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime())).toList();
    }
    
    @Override
    public void userOnline(Integer userId, String sessionId) {
        if (redisTemplate == null) {
            logger.warn("Redis未配置，无法管理在线用户状态");
            return;
        }
        
        User user = userService.findById(userId);
        if (user == null) {
            logger.warn("用户不存在: {}", userId);
            return;
        }
        
        OnlineUser onlineUser = new OnlineUser(userId, user.getUsername(), user.getNickname(), sessionId);
        
        try {
            String userJson = objectMapper.writeValueAsString(onlineUser);
            String key = ONLINE_USER_PREFIX + userId;
            redisTemplate.opsForValue().set(key, userJson, ONLINE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            logger.info("用户上线: {} ({})", user.getUsername(), sessionId);
        } catch (JsonProcessingException e) {
            logger.error("序列化在线用户信息失败", e);
        }
    }
    
    @Override
    public void userOffline(Integer userId, String sessionId) {
        if (redisTemplate == null) {
            return;
        }
        
        String key = ONLINE_USER_PREFIX + userId;
        String userJson = redisTemplate.opsForValue().get(key);
        
        if (userJson != null) {
            try {
                OnlineUser onlineUser = objectMapper.readValue(userJson, OnlineUser.class);
                // 只有会话ID匹配时才下线（防止多设备登录冲突）
                if (sessionId.equals(onlineUser.getSessionId())) {
                    redisTemplate.delete(key);
                    logger.info("用户下线: {} ({})", onlineUser.getUsername(), sessionId);
                }
            } catch (JsonProcessingException e) {
                logger.error("反序列化在线用户信息失败", e);
            }
        }
    }
    
    @Override
    public List<OnlineUser> getOnlineUsers() {
        if (redisTemplate == null) {
            return new ArrayList<>();
        }
        
        Set<String> keys = redisTemplate.keys(ONLINE_USER_PREFIX + "*");
        List<OnlineUser> onlineUsers = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                String userJson = redisTemplate.opsForValue().get(key);
                if (userJson != null) {
                    try {
                        OnlineUser onlineUser = objectMapper.readValue(userJson, OnlineUser.class);
                        onlineUsers.add(onlineUser);
                    } catch (JsonProcessingException e) {
                        logger.error("反序列化在线用户信息失败: {}", key, e);
                    }
                }
            }
        }
        
        return onlineUsers;
    }
    
    @Override
    public boolean isUserOnline(Integer userId) {
        if (redisTemplate == null) {
            return false;
        }
        
        String key = ONLINE_USER_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public void updateUserActiveTime(Integer userId) {
        if (redisTemplate == null) {
            return;
        }
        
        String key = ONLINE_USER_PREFIX + userId;
        String userJson = redisTemplate.opsForValue().get(key);
        
        if (userJson != null) {
            try {
                OnlineUser onlineUser = objectMapper.readValue(userJson, OnlineUser.class);
                onlineUser.updateLastActiveTime();
                
                String updatedJson = objectMapper.writeValueAsString(onlineUser);
                redisTemplate.opsForValue().set(key, updatedJson, ONLINE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                logger.error("更新用户活跃时间失败: {}", userId, e);
            }
        }
    }
    
    @Override
    public void cleanExpiredOnlineUsers() {
        if (redisTemplate == null) {
            return;
        }
        
        Set<String> keys = redisTemplate.keys(ONLINE_USER_PREFIX + "*");
        int cleanedCount = 0;
        
        if (keys != null) {
            for (String key : keys) {
                String userJson = redisTemplate.opsForValue().get(key);
                if (userJson != null) {
                    try {
                        OnlineUser onlineUser = objectMapper.readValue(userJson, OnlineUser.class);
                        LocalDateTime expireTime = onlineUser.getLastActiveTime().plusMinutes(ONLINE_TIMEOUT_MINUTES);
                        
                        if (LocalDateTime.now().isAfter(expireTime)) {
                            redisTemplate.delete(key);
                            cleanedCount++;
                        }
                    } catch (JsonProcessingException e) {
                        logger.error("清理过期用户时反序列化失败: {}", key, e);
                        // 如果反序列化失败，直接删除
                        redisTemplate.delete(key);
                        cleanedCount++;
                    }
                }
            }
        }
        
        if (cleanedCount > 0) {
            logger.info("清理过期在线用户: {} 个", cleanedCount);
        }
    }
}
