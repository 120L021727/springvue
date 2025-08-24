package com.example.mdtoword.config;

import com.example.mdtoword.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类 - 纯JWT认证模式
 * 
 * 重构说明：
 * 1. 移除了表单登录相关配置
 * 2. 移除了自定义认证处理器依赖
 * 3. 简化为纯JWT认证模式
 * 4. 保持CORS配置支持前后端分离
 * 
 * 认证流程：
 * 客户端 -> AuthController(/api/auth/login) -> JWT Token -> 后续请求携带Token
 * 
 * @author 坤坤
 * @since 2025-01-20
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 配置认证管理器
     * 用于处理用户名密码认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * 配置安全过滤器链 - 纯JWT认证模式
     * 
     * 配置原则：
     * 1. 完全无状态 (STATELESS) - 不使用Session
     * 2. 纯JWT认证 - 移除表单登录配置
     * 3. RESTful API - 所有接口都是API端点
     * 4. 统一的权限控制 - 通过JWT Token验证身份
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF - JWT本身就防CSRF，且前后端分离不需要CSRF保护
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置CORS跨域 - 支持前后端分离
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 认证相关接口允许匿名访问
                .requestMatchers("/api/auth/**").permitAll()
                
                // 健康检查接口允许匿名访问
                .requestMatchers("/api/converter/health").permitAll()
                
                // 静态资源允许匿名访问（用于前台展示）
                .requestMatchers("/api/file/avatar/**").permitAll()
                .requestMatchers("/api/file/rte/**").permitAll()
                
                // WebSocket连接端点允许访问（JWT认证在连接时处理）
                .requestMatchers("/ws/chat/**").permitAll()
                
                // 其他所有请求都需要JWT认证
                .anyRequest().authenticated()
            )
            
            // 配置会话管理为完全无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 添加JWT认证过滤器 - 在用户名密码认证过滤器之前执行
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * 配置CORS跨域策略
     * 允许前端应用访问后端API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的前端地址
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带认证信息（Cookie、Authorization头等）
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}