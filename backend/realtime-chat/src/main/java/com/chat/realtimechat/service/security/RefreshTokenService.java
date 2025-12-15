package com.chat.realtimechat.service.security;

import com.chat.realtimechat.model.entity.auth.RefreshToken;


public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    String generateNewJwtToken(String requestToken);
}
