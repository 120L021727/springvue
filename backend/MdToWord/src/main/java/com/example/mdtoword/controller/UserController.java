package com.example.mdtoword.controller;

import com.example.mdtoword.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private final UserService userService;

//This is a constructor for the UserController class, which takes in a UserService object as a parameter
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
