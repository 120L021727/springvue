package com.example.mdtoword.service;

import com.example.mdtoword.pojo.User;

/**
 * 认证服务接口
 * 处理用户登录和获取当前登录用户信息
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败返回null
     */
    User login(String username, String password);
    
    /**
     * 获取当前登录用户
     * @return 当前登录的用户信息
     */
    User getCurrentUser();
}