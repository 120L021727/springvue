package com.example.mdtoword.controller;

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
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String username, 
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户名是否已存在
        User existingUser = userService.findByUserName(username);
        if (existingUser != null) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 注册新用户
        userService.register(username, password);
        
        response.put("success", true);
        response.put("message", "注册成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户登录 - JWT版本
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username, 
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用Spring Security进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 生成JWT Token
            String token = jwtUtil.generateToken(username);
            
            // 获取用户信息
            User user = userService.findByUserName(username);
            
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();
        
        // 从SecurityContext获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String username = authentication.getName();
            User user = userService.findByUserName(username);
            
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
    }
    
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // JWT是无状态的，客户端删除token即可
        // 这里可以添加token黑名单逻辑（可选）
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "退出成功");
        return ResponseEntity.ok(response);
    }
}
