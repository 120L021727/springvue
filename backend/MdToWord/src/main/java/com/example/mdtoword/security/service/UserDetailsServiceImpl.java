package com.example.mdtoword.security.service;

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
 * Spring Security 用户详情服务
 * 负责从数据库加载用户信息并转换为Spring Security可用的UserDetails对象
 * 
 * 这是Spring Security认证过程中的核心组件，当用户尝试登录时，
 * Spring Security会调用这个服务来加载用户信息并验证密码
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    /**
     * 根据用户名加载用户详情
     * 
     * @param username 用户名
     * @return UserDetails对象，包含用户信息和权限
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 调用业务服务查询用户信息
        User user = userService.findByUserName(username);
        
        // 如果用户不存在，抛出Spring Security标准异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 将业务User对象转换为Spring Security的UserDetails对象
        // 这里简化处理，给所有用户分配ROLE_USER角色
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),                    // 用户名
            user.getPassword(),                    // 已加密的密码
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))  // 用户角色
        );
    }
} 