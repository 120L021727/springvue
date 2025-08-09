package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    // 认证相关逻辑已迁移到Spring Security表单登录与处理器，不在此控制器中单测

    @InjectMocks
    private UserController userController;

    @Test
    void testRegisterSuccess() {
        // Given
        String username = "testuser";
        String password = "password";
        when(userService.findByUserName(username)).thenReturn(null);
        doNothing().when(userService).register(username, password);

        // When
        ResponseEntity<Result<String>> response = userController.register(username, password);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("注册成功", response.getBody().getMessage());
    }

    @Test
    void testRegisterUserExists() {
        // Given
        String username = "existinguser";
        String password = "password";
        doThrow(new com.example.mdtoword.exception.BusinessException("用户名已存在"))
                .when(userService).register(username, password);

        // When
        ResponseEntity<Result<String>> response = userController.register(username, password);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getSuccess());
    }

    @Test
    // 登录由Security处理，不在此控制器中测试

    @Test
    void testLogoutSuccess() {
        // When
        ResponseEntity<Result<String>> response = userController.logout();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("登出成功", response.getBody().getMessage());
    }
} 