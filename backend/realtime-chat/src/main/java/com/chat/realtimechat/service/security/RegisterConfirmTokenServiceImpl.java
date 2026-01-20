package com.chat.realtimechat.service.security;

import com.chat.realtimechat.exception.auth.TokenExpiredException;
import com.chat.realtimechat.exception.auth.TokenNotFoundException;
import com.chat.realtimechat.exception.auth.UserAlreadyExistsException;
import com.chat.realtimechat.exception.auth.UserNotFoundException;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.entity.auth.RegisterConfirmToken;
import com.chat.realtimechat.repository.security.RegisterConfirmTokenRepository;
import com.chat.realtimechat.repository.users.UserRepository;
import com.chat.realtimechat.service.notifiers.UserEmailNotifier;
import com.chat.realtimechat.service.notifiers.UserEmailNotifierImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterConfirmTokenServiceImpl implements RegisterConfirmTokenService {
    @Value("${auth.registerConfirmTokenExpirationMs}")
    private Long registerConfirmTokenExpirationMs;
    @Value("${url.base}")
    String baseUrl;

    private final RegisterConfirmTokenRepository registerConfirmTokenRepository;
    private final UserEmailNotifier userEmailNotifier;
    private final UserRepository userRepository;

    @Override
    public RegisterConfirmToken createRegisterConfirmToken(Long userId) {
        var token = new RegisterConfirmToken();
        User user =  userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found!")
        );
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(registerConfirmTokenExpirationMs));
        token.setUser(user);

        return registerConfirmTokenRepository.save(token);
    }

    public User getUserByToken(String token) {
        var registerConfirmToken = registerConfirmTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        return registerConfirmToken.getUser();
    }

    @Override
    public void confirmUser(String token) {
        var registerConfirmToken = registerConfirmTokenRepository.findByToken(token).orElseThrow(
                () -> new TokenNotFoundException("Token not found")
        );
        if (registerConfirmToken.isTokenExpired()) {
            throw new TokenExpiredException("Token expired");
        }
        User user = registerConfirmToken.getUser();
        user.setConfirmed(true);
        userRepository.save(user);
    }

    private String buildUrl(String token) {
        return baseUrl + "/user/confirm/" + token;
    }

    @Override
    public void sendConfirmationEmail(RegisterConfirmToken registerConfirmToken, String userEmail) {
        userEmailNotifier.sendMessage(
                "To confirm your account please click this link: " + buildUrl(registerConfirmToken.getToken()),
                "Account Confirmation",
                userEmail
        );
    }

    @Override
    @Transactional
    public void resendConfirmationToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );

        if (user.getConfirmed()) {
            throw new UserAlreadyExistsException("Account is already active!");
        }

        registerConfirmTokenRepository.deleteByUser(user);
        registerConfirmTokenRepository.flush();

        RegisterConfirmToken newToken = createRegisterConfirmToken(user.getId());
        sendConfirmationEmail(newToken, email);
    }
}
