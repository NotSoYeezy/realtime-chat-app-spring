package com.chat.realtimechat.controller;

import com.chat.realtimechat.domain.MyUsr;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/adduser")
public class UserController {
    @PostMapping
    public String addUser(@RequestBody MyUsr newMyUsr) {
        MyUsr myUsr = new MyUsr();
        myUsr.setUsername(newMyUsr.getUsername());
//        user.setName(newUser.getName());
//        user.setSurname(newUser.getSurname());
//        user.setEmail(newUser.getEmail());
//        user.setPassword(newUser.getPassword());

        return myUsr.getUsername();
    }

}

