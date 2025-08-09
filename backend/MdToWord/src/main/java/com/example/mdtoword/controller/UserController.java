package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户注册、登录、信息管理等接口
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
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
        
        try {
            userService.register(username, password);
            return ResponseEntity.ok(Result.success("注册成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("注册失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Result<User>> getCurrentUser() {
        try {
            User user = securityUtil.getCurrentUser();
            return ResponseEntity.ok(Result.success(user));
        } catch (Exception e) {
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
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.ok(Result.notFound("用户不存在"));
        }
        user.setPassword(null);
        return ResponseEntity.ok(Result.success(user));
    }
    
    /**
     * 更新用户基本信息
     * 
     * @param user 用户信息（包含昵称、邮箱等）
     * @return 更新结果
     */
    @PutMapping("/info")
    public ResponseEntity<Result<String>> updateUserInfo(@Validated(User.UpdateInfoGroup.class) @RequestBody User user) {
        try {
            // 获取当前登录用户
            User currentUser = securityUtil.getCurrentUser();
            
            // 设置用户ID，确保更新的是当前用户
            user.setId(currentUser.getId());
            user.setUsername(currentUser.getUsername()); // 保持用户名不变
            user.setPassword(currentUser.getPassword()); // 保持密码不变
            
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
        
        try {
            // 获取当前登录用户
            User currentUser = securityUtil.getCurrentUser();
            
            // 调用服务层修改密码
            boolean success = userService.changePassword(currentPassword, newPassword);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "密码修改成功，请重新登录" : "密码修改失败");
            result.put("needRelogin", success);
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密码修改失败：" + e.getMessage());
            result.put("needRelogin", false);
            
            return ResponseEntity.badRequest().body(Result.success(result));
        }
    }
    
    /**
     * 用户登出
     * 
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout() {
        // 这里可以添加登出逻辑，比如清除JWT token等
        return ResponseEntity.ok(Result.success("登出成功"));
    }
}
