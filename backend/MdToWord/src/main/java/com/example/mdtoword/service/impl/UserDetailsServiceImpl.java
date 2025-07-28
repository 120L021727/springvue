package com.example.mdtoword.service.impl;

import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义UserDetailsService实现
 * 负责从数据库加载用户信息并转换为Spring Security可用的UserDetails对象
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    /**
     * 根据用户名加载用户详情
     * 这是Spring Security认证过程中的核心方法
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 调用我们自己的服务查询用户
        User user = userService.findByUserName(username);
        
        // 如果用户不存在，抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 将我们的User对象转换为Spring Security的UserDetails对象
        // 这里简化处理，只给用户一个基本角色
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(), // 已加密的密码
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}