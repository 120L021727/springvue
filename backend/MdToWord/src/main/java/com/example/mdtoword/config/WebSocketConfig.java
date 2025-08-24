package com.example.mdtoword.config;

import com.example.mdtoword.security.websocket.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 配置类
 * 
 * 功能设计：
 * 1. 启用WebSocket消息代理
 * 2. 配置STOMP端点和消息路由
 * 3. 集成JWT认证拦截器
 * 4. 支持跨域连接
 * 
 * 路由设计：
 * - /ws/chat: WebSocket连接端点
 * - /app/*: 客户端发送消息的前缀
 * - /topic/public: 公共房间广播
 * - /queue/private: 私聊消息队列
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 配置消息代理
     * 
     * /topic: 用于公共房间广播（一对多）
     * /queue: 用于私聊消息（一对一）
     * /app: 客户端发送消息的目标前缀
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，处理 /topic 和 /queue 前缀的消息
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 设置客户端发送消息的目标前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目标前缀，用于私聊
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 
     * 客户端通过此端点建立WebSocket连接
     * 支持SockJS降级方案，确保浏览器兼容性
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins("http://localhost:5173") // 允许前端地址访问
                .withSockJS(); // 启用SockJS支持
    }

    /**
     * 配置客户端入站通道拦截器
     * 
     * 在此处添加JWT认证拦截器，确保WebSocket连接的安全性
     * 每个STOMP消息都会经过此拦截器验证
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtAuthenticationInterceptor);
    }
}
