package com.chat.realtimechat.service.security;

import com.chat.realtimechat.model.entity.RefreshToken;
import org.springframework.stereotype.Service;


public interface RefreshTokenService {
    public RefreshToken createRefreshToken(Long userId);
    public boolean isTokenExpired(RefreshToken refreshToken);
}
