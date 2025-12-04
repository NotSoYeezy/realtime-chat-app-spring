package com.chat.realtimechat.service.security;

import com.chat.realtimechat.exception.IncorrectRefreshTokenException;
import com.chat.realtimechat.exception.RefreshTokenExpiredException;
import com.chat.realtimechat.model.entity.RefreshToken;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
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
        token.setUser(userRepository.findById(userId).get());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(token);
    }

    @Override
    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

    @Override
    public String generateNewJwtToken(String requestToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken).orElseThrow(
                () -> new IncorrectRefreshTokenException("Refresh token not found")
        );
        if (isTokenExpired(refreshToken)) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException("Refresh token expired");
        }
        return jwtUtil.generateToken(refreshToken.getUser());
    }
}
