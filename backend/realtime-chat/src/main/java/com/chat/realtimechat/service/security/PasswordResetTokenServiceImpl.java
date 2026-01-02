package com.chat.realtimechat.service.security;

import com.chat.realtimechat.exception.friendship.UserNotFoundException;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.entity.auth.PasswordResetToken;
import com.chat.realtimechat.repository.PasswordResetTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.notifiers.UserEmailNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final UserEmailNotifier userEmailNotifier;

    @Value("${auth.passwordResetTokenExpirationMs}")
    private Long passwordResetTokenExpirationMs;

    @Value("${url.base}")
    private String baseUrl;

    @Override
    public PasswordResetToken createPasswordResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(passwordResetTokenExpirationMs));
        token.setUser(user);

        return passwordResetTokenRepository.save(token);
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        PasswordResetToken token = createPasswordResetToken(user);

        userEmailNotifier.sendMessage(
                """
                To reset your password, please click the link below:
                %s

                If you did not request this, please ignore this email.
                """.formatted(buildUrl(token.getToken())),
                "Password Reset",
                email
        );
    }

    private String buildUrl(String token) {
        return baseUrl + "/auth/password-reset/confirm/" + token;
    }


}
