package com.example.mdtoword.service.impl;

import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {


    @Override
    public User findByUserName(String username) {
        return null;
    }

    @Override
    public void register(String username, String password) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void updateAvatar(String avatarUrl) {

    }

    @Override
    public void updatePwd(String newPwd) {

    }
}
