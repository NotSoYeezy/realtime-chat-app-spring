package com.chat.realtimechat.controller;


import com.chat.realtimechat.domain.User;
import com.chat.realtimechat.model.dto.request.LoginRequest;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.model.dto.response.LoginResponse;
import com.chat.realtimechat.util.JwtUtil;
import com.chat.realtimechat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid RegistrationRequest req) {
        User registeredUser = userService.registerUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        User loginUser = userService.authenticate(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(loginUser);
        LoginResponse response = new LoginResponse(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/update")
    public ResponseEntity<User> update(@RequestBody @Valid UpdateRequest req,
                                       @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        User updateUser = userService.updateUser(req, username);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updateUser);
    }
}
