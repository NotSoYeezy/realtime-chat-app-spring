package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendUserResponse {
    private final Long id;
    private String username;
    private String name;
    private String surname;

    public static FriendUserResponse fromEntity(User user) {
        return new FriendUserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname()
        );
    }
}
