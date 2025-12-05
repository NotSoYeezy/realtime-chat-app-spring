package com.chat.realtimechat.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class testController {
    @GetMapping("/test")
    public String test() {
        return "hello";
    }
}
