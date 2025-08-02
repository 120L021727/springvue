package com.example.mdtoword.security.handler;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义认证成功处理器
 * 当用户登录成功时，生成JWT Token并返回给前端
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        // 获取用户名
        String username = authentication.getName();
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(username);
        
        // 获取用户信息
        User user = userService.findByUserName(username);
        
        // 构建返回数据
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", token);
        loginData.put("user", user);
        
        // 设置响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 返回成功响应
        Result<Map<String, Object>> result = Result.success(loginData, "登录成功");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
} 