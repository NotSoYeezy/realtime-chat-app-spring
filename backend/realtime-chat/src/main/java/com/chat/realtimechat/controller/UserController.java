package com.chat.realtimechat.controller;

import com.chat.realtimechat.domain.User;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/adduser")
public class UserController {
    @PostMapping
    public String addUser(@RequestBody User newUser) {
        User user = new User();
        user.setUsername(newUser.getUsername());
//        user.setName(newUser.getName());
//        user.setSurname(newUser.getSurname());
//        user.setEmail(newUser.getEmail());
//        user.setPassword(newUser.getPassword());

        return user.getUsername();
    }

}

