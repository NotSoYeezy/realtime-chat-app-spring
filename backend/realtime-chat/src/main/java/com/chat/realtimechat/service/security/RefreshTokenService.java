package com.chat.realtimechat.service.security;

import com.chat.realtimechat.model.entity.RefreshToken;


public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    boolean isTokenExpired(RefreshToken refreshToken);
    String generateNewJwtToken(String requestToken);
}
