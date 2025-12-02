package com.chat.realtimechat.model.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {
    private final Long id;
    private final String username;
    private final String email;
    private final String token;
    private final String refreshToken;
}
