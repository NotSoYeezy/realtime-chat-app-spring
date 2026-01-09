package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResponse {
    private Long id;
    private String username;
    private String name;
    private String surname;

    public static UserSearchResponse fromEntity(User user) {
        return new UserSearchResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname()
        );
    }
}
