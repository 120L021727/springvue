package com.example.mdtoword.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 用于配置安全相关的规则、认证方式、密码编码器等
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器
     * 使用BCrypt强哈希算法，自动处理盐值
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     * 用于处理认证请求
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 安全过滤链配置
     * 定义URL访问权限、登录逻辑、会话管理等
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护，因为我们使用的是无状态API
                .csrf(AbstractHttpConfigurer::disable)
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有人访问注册和登录接口
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 移除 .formLogin() 配置
                // 配置会话管理
                .sessionManagement(session -> session
                        // 使用无状态会话，每个请求都需要认证
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }
}