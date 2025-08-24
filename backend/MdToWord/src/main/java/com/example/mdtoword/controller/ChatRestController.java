package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.ChatMessage;
import com.example.mdtoword.pojo.OnlineUser;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.service.ChatService;
import com.example.mdtoword.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天REST API控制器
 * 
 * 功能设计：
 * 1. 提供HTTP接口获取聊天记录
 * 2. 获取在线用户列表
 * 3. 聊天状态查询
 * 4. 配合WebSocket实现完整的聊天功能
 * 
 * 路由设计：
 * - GET /api/chat/public/messages - 获取公共房间消息
 * - GET /api/chat/private/messages/{userId} - 获取私聊记录
 * - GET /api/chat/online-users - 获取在线用户
 * - POST /api/chat/clean-expired - 清理过期用户（管理接口）
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@RestController
@RequestMapping("/api/chat")
public class ChatRestController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    /**
     * 获取公共房间最近的聊天记录
     * 
     * @param limit 限制数量，默认50条
     * @return 聊天记录列表
     */
    @GetMapping("/public/messages")
    public ResponseEntity<Result<List<ChatMessage>>> getPublicMessages(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            // 验证用户是否已登录
            securityUtil.getCurrentUserId(); // 会抛出异常如果未登录
            
            // 获取公共房间消息（默认房间ID为"public"）
            List<ChatMessage> messages = chatService.getRecentPublicMessages("public", Math.min(limit, 100));
            
            return ResponseEntity.ok(Result.success(messages, "获取公共消息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("获取公共消息失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取与指定用户的私聊记录
     * 
     * @param userId 对方用户ID
     * @param limit 限制数量，默认50条
     * @return 私聊记录列表
     */
    @GetMapping("/private/messages/{userId}")
    public ResponseEntity<Result<List<ChatMessage>>> getPrivateMessages(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            // 获取当前用户ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // 获取私聊记录
            List<ChatMessage> messages = chatService.getPrivateMessages(
                currentUserId, userId, Math.min(limit, 100));
            
            return ResponseEntity.ok(Result.success(messages, "获取私聊记录成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("获取私聊记录失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取当前在线用户列表
     * 
     * @return 在线用户列表
     */
    @GetMapping("/online-users")
    public ResponseEntity<Result<List<OnlineUser>>> getOnlineUsers() {
        try {
            // 验证用户是否已登录
            securityUtil.getCurrentUserId();
            
            List<OnlineUser> onlineUsers = chatService.getOnlineUsers();
            
            return ResponseEntity.ok(Result.success(onlineUsers, "获取在线用户成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("获取在线用户失败：" + e.getMessage()));
        }
    }
    
    /**
     * 检查指定用户是否在线
     * 
     * @param userId 用户ID
     * @return 在线状态
     */
    @GetMapping("/user/{userId}/online")
    public ResponseEntity<Result<Boolean>> checkUserOnline(@PathVariable Integer userId) {
        try {
            // 验证用户是否已登录
            securityUtil.getCurrentUserId();
            
            boolean isOnline = chatService.isUserOnline(userId);
            
            return ResponseEntity.ok(Result.success(isOnline, "检查用户在线状态成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("检查用户在线状态失败：" + e.getMessage()));
        }
    }
    
    /**
     * 清理过期的在线用户（管理接口）
     * 
     * 注意：此接口可以设置为定时任务调用，或由管理员手动触发
     * 
     * @return 清理结果
     */
    @PostMapping("/clean-expired")
    public ResponseEntity<Result<String>> cleanExpiredUsers() {
        try {
            // 验证用户是否已登录（可以增加管理员权限验证）
            securityUtil.getCurrentUserId();
            
            chatService.cleanExpiredOnlineUsers();
            
            return ResponseEntity.ok(Result.success("清理过期用户成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("清理过期用户失败：" + e.getMessage()));
        }
    }
}
