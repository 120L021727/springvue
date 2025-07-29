package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.AuthService;
import com.example.mdtoword.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String username, 
            @RequestParam String password) {
        
        // 检查用户名是否已存在
        User existingUser = userService.findByUserName(username);
        if (existingUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 注册新用户
        userService.register(username, password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "注册成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username, 
            @RequestParam String password,
            HttpServletRequest request) {
        
        // 执行登录
        User user = authService.login(username, password);
        
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            // 手动设置Spring Security认证上下文
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            // 将认证信息保存到会话中
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                           SecurityContextHolder.getContext());
            
            // 登录成功
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            // 登录失败
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取当前用户信息
     * @return 当前登录用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // 从SecurityContext获取当前用户
        User currentUser = authService.getCurrentUser();
        
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser != null) {
            response.put("success", true);
            response.put("user", currentUser);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
    }
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody User user) {
        // 获取当前登录用户
        User currentUser = authService.getCurrentUser();
        
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // 确保只能更新自己的信息
        user.setUsername(currentUser.getUsername());
        userService.update(user);
        
        response.put("success", true);
        response.put("message", "更新成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户头像
     * @param avatarUrl 头像URL
     * @return 更新结果
     */
    @PutMapping("/avatar")
    public ResponseEntity<Map<String, Object>> updateAvatar(@RequestParam String avatarUrl) {
        // 使用SecurityContextHolder获取当前用户并更新头像
        userService.updateAvatar(avatarUrl);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "头像更新成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新密码
     * @param newPwd 新密码
     * @return 更新结果
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestParam String newPwd) {
        // 使用SecurityContextHolder获取当前用户并更新密码
        userService.updatePwd(newPwd);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "密码更新成功");
        return ResponseEntity.ok(response);
    }
}
