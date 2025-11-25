package com.chat.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class testController {
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "hello";
    }
}
