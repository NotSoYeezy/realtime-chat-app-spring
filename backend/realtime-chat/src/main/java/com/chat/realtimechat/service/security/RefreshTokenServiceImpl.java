package com.chat.realtimechat.service.security;

import com.chat.realtimechat.exception.auth.TokenExpiredException;
import com.chat.realtimechat.exception.auth.TokenNotFoundException;
import com.chat.realtimechat.exception.auth.UserNotFoundException;
import com.chat.realtimechat.model.entity.auth.RefreshToken;
import com.chat.realtimechat.repository.security.RefreshTokenRepository;
import com.chat.realtimechat.repository.users.UserRepository;
import com.chat.realtimechat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final JwtUtil jwtUtil;
    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        var token = new RefreshToken();
        token.setUser(userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        ));
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(token);
    }

    @Override
    public String generateNewJwtToken(String requestToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken).orElseThrow(
                () -> new TokenNotFoundException("Token not found")
        );
        if (refreshToken.isTokenExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired");
        }
        return jwtUtil.generateToken(refreshToken.getUser());
    }
}
