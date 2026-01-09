package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.validation.ValidEmail;
import com.chat.realtimechat.validation.ValidPassword;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import lombok.Data;


@Data
public class UpdateUserRequest {
    @Null(message = "ID cannot be set by a user")
    private Long id;

    @Null(message = "Username cannot be set by a user")
    private String username;

    @Nullable
    @ValidPassword
    private String password;

    @Nullable
    @ValidEmail
    private String email;

    @Nullable
    private String name;

    @Nullable
    private String surname;

}
