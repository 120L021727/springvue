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
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 根据用户名查找用户
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
     * 注册新用户
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
     * @param user 用户对象
     */
    @Override
    public void update(User user) {
        // 更新用户信息，不更新密码
        User dbUser = findByUserName(user.getUsername());
        if (dbUser != null) {
            user.setId(dbUser.getId());
            user.setPassword(dbUser.getPassword()); // 保持原密码不变
            userMapper.updateById(user);
        }
    }

    /**
     * 更新用户头像
     * @param avatarUrl 头像URL
     */
    @Override
    public void updateAvatar(String avatarUrl) {
        // 从SecurityContextHolder获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            
            // 获取用户并更新头像
            User user = findByUserName(username);
            if (user != null) {
                user.setUserPic(avatarUrl);
                userMapper.updateById(user);
            }
        }
    }

    /**
     * 更新用户密码
     * @param newPwd 新密码（明文）
     */
    @Override
    public void updatePwd(String newPwd) {
        // 从SecurityContextHolder获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            
            // 获取用户并更新密码
            User user = findByUserName(username);
            if (user != null) {
                // 加密新密码
                user.setPassword(passwordEncoder.encode(newPwd));
                userMapper.updateById(user);
            }
        }
    }
}
