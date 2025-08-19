package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 认证控制器
 * 
 * 提供纯JWT认证的RESTful API接口
 * 替代原有的Spring Security表单登录机制
 * 
 * 设计原则：
 * 1. 统一使用JSON数据格式
 * 2. 标准的HTTP状态码
 * 3. 清晰的错误信息
 * 4. 无状态的JWT认证
 * 
 * @author 坤坤
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Value("${sso.key-prefix:sso:token}")
    private String ssoKeyPrefix;
    
    /**
     * 用户登录接口
     * 
     * 认证流程：
     * 1. 接收JSON格式的用户名密码
     * 2. 使用Spring Security的AuthenticationManager进行认证
     * 3. 认证成功后生成JWT Token
     * 4. 返回Token和用户信息
     * 
     * @param loginRequest 登录请求对象
     * @return 包含JWT Token和用户信息的响应
     */
    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 使用Spring Security进行用户名密码认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // 认证成功，获取用户名
            String username = authentication.getName();
            
            // 生成JTI并写入Redis（单点登录：仅保留最近一次登录）
            String jti = UUID.randomUUID().toString();
            if (stringRedisTemplate != null) {
                String key = ssoKeyPrefix + ":" + username;
                // 使用与JWT相同的过期时间，避免悬挂会话
                stringRedisTemplate.opsForValue().set(key, jti, jwtUtil.getExpirationDateFromToken(
                        jwtUtil.generateToken(username, jti)
                ).getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }

            // 生成包含JTI的JWT Token
            String token = jwtUtil.generateToken(username, jti);
            
            // 获取完整的用户信息（用于前端显示）
            User user = userService.findByUserName(username);
            
            // 构建响应数据
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("user", user);
            
            return ResponseEntity.ok(Result.success(loginData, "登录成功"));
            
        } catch (BadCredentialsException e) {
            // 认证失败（用户名或密码错误）
            return ResponseEntity.status(401)
                .body(Result.unauthorized("用户名或密码错误"));
        } catch (Exception e) {
            // 其他异常
            return ResponseEntity.status(500)
                .body(Result.error("登录失败：" + e.getMessage()));
        }
    }
    
    /**
     * 用户注册接口
     * 
     * 注册流程：
     * 1. 接收JSON格式的注册信息
     * 2. 验证用户名是否已存在
     * 3. 创建新用户（密码自动加密）
     * 4. 返回注册结果
     * 
     * @param registerRequest 注册请求对象（复用LoginRequest）
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<Result<String>> register(@Valid @RequestBody LoginRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            if (userService.findByUserName(registerRequest.getUsername()) != null) {
                return ResponseEntity.badRequest()
                    .body(Result.error("用户名已存在"));
            }
            
            // 创建新用户
            userService.register(registerRequest.getUsername(), registerRequest.getPassword());
            
            return ResponseEntity.ok(Result.success("注册成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Result.error("注册失败：" + e.getMessage()));
        }
    }
    
    /**
     * 登录请求数据传输对象
     * 
     * 使用内部类简化代码结构，避免创建过多的独立类文件
     * 包含必要的数据验证注解
     */
    public static class LoginRequest {
        
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
        private String password;
        
        // 构造函数
        public LoginRequest() {}
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        // Getter和Setter
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    

}
