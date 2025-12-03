package com.chat.realtimechat.model.dto.response;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {
    private final String email;
    private final String username;
    private final String name;
    private final String surname;
}
