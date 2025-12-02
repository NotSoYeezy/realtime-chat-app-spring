package com.chat.realtimechat.controller.security;


import com.chat.realtimechat.model.dto.request.RefreshTokenRequest;
import com.chat.realtimechat.model.dto.response.UserResponse;
import com.chat.realtimechat.model.entity.RefreshToken;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.dto.request.LoginRequest;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.model.dto.response.AuthTokenResponse;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.service.security.RefreshTokenService;
import com.chat.realtimechat.util.JwtUtil;
import com.chat.realtimechat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegistrationRequest req) {
        User registeredUser = userService.registerUser(req);
        String token = jwtUtil.generateToken(registeredUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(registeredUser.getId());
        UserResponse response = new UserResponse(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getEmail(),
                token,
                refreshToken.getToken()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@RequestBody @Valid LoginRequest req) {
        User loginUser = userService.authenticate(req.getEmail(), req.getPassword());
        String token = jwtUtil.generateToken(loginUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginUser.getId());
        AuthTokenResponse response = new AuthTokenResponse(token, refreshToken.getToken());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/update")
    public ResponseEntity<User> update(@RequestBody @Valid UpdateRequest req,
                                       @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        User updateUser = userService.updateUser(req, username);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updateUser);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest req) {
        String requestToken = req.getRefreshToken();
        return refreshTokenRepository.findByToken(requestToken)
                .map(token -> {
                    if (refreshTokenService.isTokenExpired(token)) {
                        refreshTokenRepository.delete(token);
                        return ResponseEntity.badRequest().body("Refresh token expired. Please login again.");
                    }
                    String newJwt = jwtUtil.generateToken(token.getUser());
                    return ResponseEntity.ok(Map.of("token", newJwt));
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid refresh token."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> payload) {
        String requestToken = payload.get("refreshToken");

        if (requestToken == null || requestToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token is required.");
        }

        return refreshTokenRepository.findByToken(requestToken)
                .map(token -> {
                    refreshTokenRepository.delete(token);
                    return ResponseEntity.ok("Logged out successfully.");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid refresh token."));
    }
}
