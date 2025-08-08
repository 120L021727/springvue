package com.example.mdtoword.controller;

import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户注册、获取当前用户信息、修改密码等功能
 * 登录功能由Spring Security表单登录处理
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
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
     * 
     * @return 当前用户信息
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
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Result<User>> getUserById(@PathVariable Integer userId) {
        try {
            System.out.println("正在查找用户ID: " + userId);
            User user = userService.findById(userId);
            if (user == null) {
                System.out.println("用户ID " + userId + " 不存在");
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("找到用户: " + user.getUsername());
            // 出于安全考虑，不返回密码等敏感信息
            user.setPassword(null);
            
            return ResponseEntity.ok(Result.success(user));
        } catch (Exception e) {
            System.err.println("获取用户信息失败，用户ID: " + userId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Result.error("获取用户信息失败"));
        }
    }
    
    /**
     * 更新用户基本信息
     * 
     * @param user 用户信息（包含昵称、邮箱等）
     * @return 更新结果
     */
    @PutMapping("/info")
    public ResponseEntity<Result<String>> updateUserInfo(@Validated(User.UpdateInfoGroup.class) @RequestBody User user) {
        // 从SecurityContext获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                .body(Result.unauthorized("用户未登录"));
        }
        
        String username = authentication.getName();
        User currentUser = userService.findByUserName(username);
        
        if (currentUser == null) {
            return ResponseEntity.badRequest()
                .body(Result.error("用户不存在"));
        }
        
        // 设置用户ID，确保更新的是当前用户
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername()); // 保持用户名不变
        user.setPassword(currentUser.getPassword()); // 保持密码不变
        
        try {
            // 调用服务层更新用户信息
            userService.update(user);
            return ResponseEntity.ok(Result.success("信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("信息更新失败：" + e.getMessage()));
        }
    }
    
    /**
     * 修改用户密码
     * 修改成功后需要重新登录
     * 
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ResponseEntity<Result<Map<String, Object>>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        
        // 参数验证
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Result.error("当前密码不能为空"));
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Result.error("新密码不能为空"));
        }
        
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Result.error("新密码长度不能少于6位"));
        }
        
        // 检查新密码是否与当前密码相同
        if (currentPassword.equals(newPassword)) {
            return ResponseEntity.badRequest()
                .body(Result.error("新密码不能与当前密码相同"));
        }
        
        // 调用服务层修改密码
        boolean success = userService.changePassword(currentPassword, newPassword);
        
        if (success) {
            // 构建返回信息，提示用户需要重新登录
            Map<String, Object> result = new HashMap<>();
            result.put("message", "密码修改成功，请重新登录");
            result.put("needRelogin", true);
            
            return ResponseEntity.ok(Result.success(result));
        } else {
            return ResponseEntity.badRequest()
                .body(Result.error("密码修改失败，请检查当前密码是否正确"));
        }
    }
    
    /**
     * 用户退出登录
     * 
     * @return 退出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout() {
        // JWT是无状态的，客户端删除token即可
        // 这里可以添加token黑名单逻辑（可选）
        
        return ResponseEntity.ok(Result.success("退出成功"));
    }
}
