package com.example.mdtoword.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 
 * 功能设计：
 * 1. 支持公共房间消息和私聊消息
 * 2. 消息类型区分（文本、系统通知等）
 * 3. 消息状态管理（已发送、已读等）
 * 4. 自动时间戳管理
 * 
 * 表结构设计：
 * - id: 消息唯一标识
 * - content: 消息内容
 * - type: 消息类型（TEXT, SYSTEM, JOIN, LEAVE）
 * - chat_type: 聊天类型（PUBLIC, PRIVATE）
 * - sender_id: 发送者ID
 * - receiver_id: 接收者ID（私聊时使用）
 * - room_id: 房间ID（公共聊天时使用）
 * - create_time: 创建时间
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Data
@TableName("tb_chat_message")
public class ChatMessage {
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT,       // 普通文本消息
        SYSTEM,     // 系统消息
        JOIN,       // 用户加入
        LEAVE       // 用户离开
    }
    
    /**
     * 聊天类型枚举
     */
    public enum ChatType {
        PUBLIC,     // 公共房间
        PRIVATE     // 私聊
    }
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @NotBlank(message = "消息内容不能为空")
    @TableField("content")
    private String content;
    
    @NotNull(message = "消息类型不能为空")
    @TableField("type")
    private MessageType type;
    
    @NotNull(message = "聊天类型不能为空")
    @TableField("chat_type")
    private ChatType chatType;
    
    @NotNull(message = "发送者ID不能为空")
    @TableField("sender_id")
    private Integer senderId;
    
    @TableField("sender_name")
    private String senderName; // 发送者用户名，用于前端显示
    
    @TableField("receiver_id")
    private Integer receiverId; // 私聊时的接收者ID
    
    @TableField("room_id")
    private String roomId; // 房间ID，暂时可以使用 "public" 作为默认公共房间
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 用于WebSocket传输的构造方法
    public ChatMessage() {}
    
    public ChatMessage(String content, MessageType type, ChatType chatType, 
                      Integer senderId, String senderName) {
        this.content = content;
        this.type = type;
        this.chatType = chatType;
        this.senderId = senderId;
        this.senderName = senderName;
    }
    
    // 创建公共消息的工厂方法
    public static ChatMessage createPublicMessage(String content, Integer senderId, String senderName) {
        ChatMessage message = new ChatMessage(content, MessageType.TEXT, ChatType.PUBLIC, senderId, senderName);
        message.setRoomId("public");
        return message;
    }
    
    // 创建私聊消息的工厂方法
    public static ChatMessage createPrivateMessage(String content, Integer senderId, String senderName, Integer receiverId) {
        ChatMessage message = new ChatMessage(content, MessageType.TEXT, ChatType.PRIVATE, senderId, senderName);
        message.setReceiverId(receiverId);
        return message;
    }
    
    // 创建系统消息的工厂方法
    public static ChatMessage createSystemMessage(String content, MessageType type) {
        ChatMessage message = new ChatMessage(content, type, ChatType.PUBLIC, null, "System");
        message.setRoomId("public");
        return message;
    }
}
