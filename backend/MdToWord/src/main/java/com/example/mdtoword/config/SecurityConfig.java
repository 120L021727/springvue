package com.example.mdtoword.config;

import com.example.mdtoword.security.filter.JwtAuthenticationFilter;
import com.example.mdtoword.security.handler.CustomAuthenticationSuccessHandler;
import com.example.mdtoword.security.handler.CustomAuthenticationFailureHandler;
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
 * Spring Security 安全配置类
 * 配置表单登录、JWT认证、CORS等安全功能
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    
    /**
     * 配置认证管理器
     * 用于处理用户名密码认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * 配置安全过滤器链
     * 启用表单登录 + JWT认证的混合模式
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF，因为使用JWT
            .csrf(AbstractHttpConfigurer::disable)
            // 配置CORS跨域
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许注册接口匿名访问
                .requestMatchers("/api/user/register").permitAll()
                // 健康检查接口允许匿名访问
                .requestMatchers("/api/converter/health").permitAll()
                // 允许头像与富文本图片匿名访问（用于前台展示）
                .requestMatchers("/api/file/avatar/**").permitAll()
                .requestMatchers("/api/file/rte/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 配置表单登录
            .formLogin(form -> form
                .loginProcessingUrl("/api/user/login")  // 登录处理URL
                .usernameParameter("username")          // 用户名参数
                .passwordParameter("password")          // 密码参数
                .successHandler(authenticationSuccessHandler)  // 登录成功处理器
                .failureHandler(authenticationFailureHandler)  // 登录失败处理器
                .permitAll()  // 允许所有人访问登录页面
            )
            // 配置会话管理为无状态（JWT）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 添加JWT认证过滤器
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