package com.example.mdtoword.security.handler;

import com.example.mdtoword.pojo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义认证失败处理器
 * 当用户登录失败时，返回友好的错误信息
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException, ServletException {
        
        // 设置响应头
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 返回失败响应
        Result<String> result = Result.unauthorized("用户名或密码错误");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
} 