package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.util.JwtUtil;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

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
        User existingUser = new User();
        existingUser.setUsername(username);
        when(userService.findByUserName(username)).thenReturn(existingUser);

        // When & Then
        assertThrows(com.example.mdtoword.exception.BusinessException.class, () -> {
            userController.register(username, password);
        });
    }

    @Test
    void testLoginSuccess() {
        // Given
        String username = "testuser";
        String password = "password";
        String token = "jwt-token";
        User user = new User();
        user.setUsername(username);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(username)).thenReturn(token);
        when(userService.findByUserName(username)).thenReturn(user);

        // When
        ResponseEntity<Result<Map<String, Object>>> response = userController.login(username, password);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("登录成功", response.getBody().getMessage());
    }

    @Test
    void testLogoutSuccess() {
        // When
        ResponseEntity<Result<String>> response = userController.logout();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("退出成功", response.getBody().getMessage());
    }
} 