package com.chat.realtimechat.controller.security;


import com.chat.realtimechat.model.dto.request.*;
import com.chat.realtimechat.model.dto.response.RegisteredUserResponse;
import com.chat.realtimechat.model.entity.auth.RefreshToken;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.dto.response.AuthTokenResponse;
import com.chat.realtimechat.model.entity.auth.RegisterConfirmToken;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.RegisterConfirmTokenRepository;
import com.chat.realtimechat.service.security.RefreshTokenService;
import com.chat.realtimechat.service.security.RegisterConfirmTokenService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    //TODO: move methods implementations into services

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RegisterConfirmTokenRepository registerConfirmTokenRepository;
    private final RegisterConfirmTokenService registerConfirmTokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponse> register(@RequestBody @Valid RegistrationRequest req) {
        User registeredUser = userService.registerUser(req);
        RegisterConfirmToken registerConfirmToken =
                registerConfirmTokenService.createRegisterConfirmToken(registeredUser.getId());
        registerConfirmTokenService.sendConfirmationEmail(registerConfirmToken, registeredUser.getEmail());
        RegisteredUserResponse response = new RegisteredUserResponse(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getSurname(),
                registeredUser.getEmail()
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

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody @Valid UpdateUserRequest req,
                                       @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        userService.updateUser(req, username);
        return ResponseEntity.ok("You changed your credentials");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest req) {
        String reqToken = req.getRefreshToken();
        String newJwt = refreshTokenService.generateNewJwtToken(reqToken);
        AuthTokenResponse response = new AuthTokenResponse(newJwt, reqToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal UserDetails myUser) {
        Optional<User> user = userService.findUsersByUsername(myUser.getUsername());
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get().getId());
        }
        return ResponseEntity.badRequest().body("User not found.");
    }

    @PostMapping("/checkPassword")
    public ResponseEntity<?> checkPassword(@AuthenticationPrincipal UserDetails user, @RequestBody(required = false) Map<String, String> request) {
        String username = user.getUsername();
        String password = (request != null) ? request.get("password") : null;

        boolean check = userService.checkPassword(username, password);
        if (check) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("hasPassword", true));
        }
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("hasPassword", false));
    }

    @GetMapping("/confirm/{token}")
    public ResponseEntity<AuthTokenResponse> confirmRegistration(@PathVariable String token) {
        registerConfirmTokenService.confirmUser(token);
        User user = registerConfirmTokenService.getUserByToken(token);
        String jwtToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        AuthTokenResponse response = new AuthTokenResponse(jwtToken, refreshToken.getToken());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmation(@RequestBody ResendConfirmationEmailRequest request) {
        String email = request.getEmail();

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required.");
        }
        registerConfirmTokenService.resendConfirmationToken(email);
        return ResponseEntity.status(HttpStatus.OK).body("Confirmation Token has been sent.");
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody ResetPasswordRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("Password reset instructions have been sent to your email.");
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> resetPassword(@RequestBody ConfirmPasswordResetRequest request) {
        userService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body("Password has been reset successfully.");
    }
}
