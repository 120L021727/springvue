package com.example.mdtoword.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户状态实体类
 * 
 * 设计说明：
 * 1. 此类不对应数据库表，仅用于Redis存储和内存管理
 * 2. 记录用户在线状态、最后活跃时间等信息
 * 3. 支持WebSocket会话管理
 * 
 * Redis存储结构：
 * - Key: "online:user:{userId}"
 * - Value: JSON格式的OnlineUser对象
 * - TTL: 自动过期时间（如5分钟无活动则过期）
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Data
public class OnlineUser {
    
    /**
     * 用户状态枚举
     */
    public enum Status {
        ONLINE,     // 在线
        AWAY,       // 离开
        BUSY,       // 忙碌
        OFFLINE     // 离线
    }
    
    private Integer userId;          // 用户ID
    private String username;         // 用户名
    private String nickname;         // 昵称
    private String sessionId;        // WebSocket会话ID
    private Status status;           // 在线状态
    private LocalDateTime lastActiveTime; // 最后活跃时间
    private LocalDateTime loginTime; // 登录时间
    private String roomId;          // 当前所在房间ID
    
    public OnlineUser() {}
    
    public OnlineUser(Integer userId, String username, String nickname, String sessionId) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.sessionId = sessionId;
        this.status = Status.ONLINE;
        this.lastActiveTime = LocalDateTime.now();
        this.loginTime = LocalDateTime.now();
        this.roomId = "public"; // 默认进入公共房间
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * 设置用户状态
     */
    public void setStatusAndUpdateTime(Status status) {
        this.status = status;
        this.lastActiveTime = LocalDateTime.now();
    }
}
