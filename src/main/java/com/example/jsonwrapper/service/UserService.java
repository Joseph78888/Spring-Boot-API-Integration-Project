package com.example.jsonwrapper.service;

import com.example.jsonwrapper.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserGateway userGateway;

    public UserService(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public User[] getUsers(){
        return userGateway.getUsers();
    }
}
