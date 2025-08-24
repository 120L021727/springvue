package com.example.mdtoword.security.websocket;

import com.example.mdtoword.security.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;

/**
 * WebSocket JWT认证拦截器
 * 
 * 功能：
 * 1. 拦截WebSocket STOMP消息
 * 2. 验证JWT Token
 * 3. 设置用户认证信息
 * 4. 支持SSO验证
 * 
 * 认证流程：
 * 客户端连接时在Header中携带JWT Token
 * 拦截器验证Token并设置用户身份
 * 后续消息自动关联到已认证用户
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Component
public class JwtAuthenticationInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Value("${sso.key-prefix:sso:token}")
    private String ssoKeyPrefix;

    /**
     * 消息发送前拦截处理
     * 
     * 主要处理CONNECT类型的消息，进行JWT认证
     * 其他类型消息直接放行（已认证用户的后续操作）
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 处理WebSocket连接认证
            String token = getTokenFromHeaders(accessor);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    // SSO验证：检查Redis中的JTI是否匹配
                    if (stringRedisTemplate != null) {
                        String jti = jwtUtil.getJtiFromToken(token);
                        String key = ssoKeyPrefix + ":" + username;
                        String jtiInRedis = stringRedisTemplate.opsForValue().get(key);
                        
                        if (jtiInRedis == null || !jtiInRedis.equals(jti)) {
                            logger.warn("WebSocket SSO验证失败，用户: {}", username);
                            return null; // 阻止连接
                        }
                    }
                    
                    // 加载用户详情并设置认证信息
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    accessor.setUser(authentication);
                    logger.info("WebSocket JWT认证成功，用户: {}", username);
                    
                } catch (Exception ex) {
                    logger.warn("WebSocket JWT认证失败: {}", ex.getMessage());
                    return null; // 阻止连接
                }
            } else {
                logger.warn("WebSocket连接缺少有效的JWT Token");
                return null; // 阻止连接
            }
        }
        
        return message;
    }

    /**
     * 从STOMP Headers中提取JWT Token
     * 
     * 支持两种方式：
     * 1. Authorization Header: "Bearer <token>"
     * 2. 自定义Header: "X-Auth-Token: <token>"
     */
    private String getTokenFromHeaders(StompHeaderAccessor accessor) {
        // 方式1: 从Authorization header中获取
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 方式2: 从自定义header中获取
        String tokenHeader = accessor.getFirstNativeHeader("X-Auth-Token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }
        
        return null;
    }
}
