package com.example.mdtoword.util;

import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 权限管理工具类
 * 统一处理用户认证和权限验证逻辑
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Component
public class SecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前登录用户ID
     * 
     * 实现逻辑：
     * 1. 从Spring Security上下文获取认证信息
     * 2. 根据用户名查询用户表获取用户ID
     * 3. 确保返回正确的用户ID
     * 
     * @return 用户ID
     * @throws BusinessException 当用户未登录或不存在时抛出异常
     */
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("用户未登录");
        }
        String username = authentication.getName();
        User user = userService.findByUserName(username);
        if (user == null) {
            logger.error("用户不存在: {}", username);
            throw new BusinessException("用户不存在");
        }
        return user.getId();
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * 实现逻辑：
     * 1. 从Spring Security上下文获取认证信息
     * 2. 根据用户名查询完整的用户信息
     * 3. 返回用户对象
     * 
     * @return 用户信息
     * @throws BusinessException 当用户未登录或不存在时抛出异常
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("用户未登录");
        }
        String username = authentication.getName();
        User user = userService.findByUserName(username);
        if (user == null) {
            logger.error("用户不存在: {}", username);
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    /**
     * 检查用户是否已登录
     * 
     * 实现逻辑：
     * 1. 从Spring Security上下文获取认证信息
     * 2. 检查认证状态和用户名
     * 3. 返回登录状态
     * 
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getName());
    }
    
    /**
     * 检查是否为指定用户
     * 
     * 实现逻辑：
     * 1. 获取当前登录用户ID
     * 2. 与指定用户ID进行比较
     * 3. 返回比较结果
     * 
     * @param userId 要检查的用户ID
     * @return 是否为指定用户
     */
    public boolean isCurrentUser(Integer userId) {
        try {
            Integer currentUserId = getCurrentUserId();
            return currentUserId.equals(userId);
        } catch (BusinessException e) {
            return false;
        }
    }
} 