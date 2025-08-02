package com.example.mdtoword.security.filter;

import com.example.mdtoword.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 
 * 这个过滤器会在每个请求中检查JWT Token，如果Token有效，
 * 就将用户信息设置到Spring Security的上下文中，实现无状态认证
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * 过滤器核心方法
     * 处理每个HTTP请求，验证JWT Token并设置认证信息
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求头中提取JWT Token
            String token = getTokenFromRequest(request);
            
            // 如果Token存在且有效
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 从Token中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);
                
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 创建Spring Security认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 将认证信息设置到Spring Security上下文中
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("JWT认证成功，用户: {}", username);
            }
        } catch (Exception ex) {
            // 记录认证失败日志，但不阻止请求继续
            logger.warn("JWT认证失败: {}", ex.getMessage());
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从HTTP请求头中提取JWT Token
     * 
     * @param request HTTP请求对象
     * @return JWT Token字符串，如果不存在返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 去掉"Bearer "前缀
        }
        return null;
    }
    
    /**
     * 判断是否应该跳过JWT认证
     * 对于公开的接口（如登录、注册），不需要JWT认证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 跳过登录、注册、健康检查等公开接口
        return path.startsWith("/api/user/register") || 
               path.startsWith("/api/user/login") ||
               path.startsWith("/api/converter/health");
    }
} 