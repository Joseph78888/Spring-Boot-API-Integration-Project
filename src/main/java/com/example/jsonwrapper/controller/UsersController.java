package com.example.jsonwrapper.controller;

import com.example.jsonwrapper.model.User;
import com.example.jsonwrapper.service.PostGateway;
import com.example.jsonwrapper.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public User[] getUsers(){
        return userService.getUsers();
    }

}
