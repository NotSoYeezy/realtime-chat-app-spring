package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private final Long id;
    private final String email;
    private String username;
    private String name;
    private String surname;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getSurname()
        );
    }
}
