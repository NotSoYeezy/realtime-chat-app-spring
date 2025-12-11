package com.chat.realtimechat.model.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RegisteredUserResponse {
    private final Long id;
    private final String name;
    private final String surname;
    private final String email;
    private final String token;
    private final String refreshToken;
}
