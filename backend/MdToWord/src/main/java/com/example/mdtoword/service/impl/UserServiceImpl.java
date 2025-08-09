package com.example.mdtoword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mdtoword.mapper.UserMapper;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 处理用户相关的业务逻辑
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在返回null
     */
    @Override
    public User findByUserName(String username) {
        // 使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户对象，如果不存在返回null
     */
    @Override
    public User findById(Integer userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 注册新用户
     * 
     * @param username 用户名
     * @param password 密码（明文）
     */
    @Override
    public void register(String username, String password) {
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(username); // 默认昵称与用户名相同
        user.setEmail(username + "@example.com"); // 默认邮箱
        
        // 保存用户
        userMapper.insert(user);
    }

    /**
     * 更新用户信息
     * 
     * @param user 用户对象
     */
    @Override
    public void update(User user) {
        // 直接更新用户信息，传入的user对象应该已经包含了正确的ID
        if (user.getId() != null) {
            userMapper.updateById(user);
        } else {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }

    /**
     * 更新用户头像
     * 
     * @param avatarUrl 头像URL
     */
    @Override
    public void updateAvatar(String avatarUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        String username = authentication.getName();
        User user = findByUserName(username);
        if (user == null) {
            return;
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPic(avatarUrl);
        userMapper.updateById(updateUser);
    }

    /**
     * 修改用户密码
     * 验证当前密码，更新为新密码
     * 
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 修改结果，true-成功，false-失败
     */
    @Override
    public boolean changePassword(String currentPassword, String newPassword) {
        // 从SecurityContextHolder获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        User user = findByUserName(username);
        
        if (user == null) {
            return false;
        }
        
        // 验证当前密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        // 检查新密码是否与当前密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return false; // 新密码与当前密码相同，返回失败
        }
        
        // 创建新的User对象进行更新，不设置updateTime，让MyBatis Plus自动填充
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        // 不手动设置updateTime，让MyBatis Plus的自动填充机制工作
        
        userMapper.updateById(updateUser);
        
        return true;
    }
}
