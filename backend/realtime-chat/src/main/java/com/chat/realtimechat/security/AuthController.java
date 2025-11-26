package com.chat.realtimechat.security;


import com.chat.realtimechat.domain.User;
import com.chat.realtimechat.model.dto.request.LoginRequset;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.response.LoginResponse;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jackson.JacksonComponentModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid RegistrationRequest req) {
        User registeredUser = userService.registerUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequset req) {
        User loginUser = userService.authenticate(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(loginUser.getUsername());
        LoginResponse response = new LoginResponse(token);
        return ResponseEntity.ok(response);
    }
}
