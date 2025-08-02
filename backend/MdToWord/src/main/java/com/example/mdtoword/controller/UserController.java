package com.example.mdtoword.controller;

import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理用户注册、获取当前用户信息、退出登录等功能
 * 登录功能由Spring Security表单登录处理
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Result<String>> register(
            @RequestParam String username, 
            @RequestParam String password) {
        
        // 检查用户名是否已存在
        User existingUser = userService.findByUserName(username);
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 注册新用户
        userService.register(username, password);
        
        return ResponseEntity.ok(Result.success("注册成功"));
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Result<User>> getCurrentUser() {
        // 从SecurityContext获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String username = authentication.getName();
            User user = userService.findByUserName(username);
            
            return ResponseEntity.ok(Result.success(user));
        } else {
            return ResponseEntity.status(401)
                .body(Result.unauthorized("未登录"));
        }
    }
    
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout() {
        // JWT是无状态的，客户端删除token即可
        // 这里可以添加token黑名单逻辑（可选）
        
        return ResponseEntity.ok(Result.success("退出成功"));
    }
}
