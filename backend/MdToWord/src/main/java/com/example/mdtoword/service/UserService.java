package com.example.mdtoword.service;

import com.example.mdtoword.pojo.User;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
public interface UserService {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在返回null
     */
    User findByUserName(String username);
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户对象，如果不存在返回null
     */
    User findById(Integer userId);
    
    /**
     * 注册新用户
     * 
     * @param username 用户名
     * @param password 密码（明文）
     */
    void register(String username, String password);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户对象
     */
    void update(User user);
    
    /**
     * 更新用户头像
     * 
     * @param avatarUrl 头像URL
     */
    void updateAvatar(String avatarUrl);
    
    /**
     * 修改用户密码
     * 验证当前密码，更新为新密码
     * 
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 修改结果，true-成功，false-失败
     */
    boolean changePassword(String currentPassword, String newPassword);
}
