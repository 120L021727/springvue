package com.example.mdtoword.controller;

import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
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
     * 用户登录 - JWT版本
     */
    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(
            @RequestParam String username, 
            @RequestParam String password) {
        
        // 移除 try-catch，让异常自然抛出给 GlobalExceptionHandler 处理
        // 使用Spring Security进行认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(username);
        
        // 获取用户信息
        User user = userService.findByUserName(username);
        
        // 构建返回数据
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", token);
        loginData.put("user", user);
        
        return ResponseEntity.ok(Result.success(loginData, "登录成功"));
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
