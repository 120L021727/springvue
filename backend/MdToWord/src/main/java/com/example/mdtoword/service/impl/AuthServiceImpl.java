package com.example.mdtoword.service.impl;

import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.AuthService;
import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 处理用户登录和获取当前登录用户信息
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录
     * 使用Spring Security的认证管理器进行认证
     */
    @Override
    public User login(String username, String password) {
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, password);
            
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 认证成功，将认证信息存入SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 返回用户信息
            return userService.findByUserName(username);
        } catch (AuthenticationException e) {
            // 认证失败
            return null;
        }
    }
    
    /**
     * 获取当前登录用户
     * 从SecurityContextHolder中获取当前认证信息
     */
    @Override
    public User getCurrentUser() {
        // 从SecurityContextHolder获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 如果已认证且不是匿名用户
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            // 获取用户名
            String username = authentication.getName();
            // 查询并返回完整的用户信息
            return userService.findByUserName(username);
        }
        
        return null;
    }
}