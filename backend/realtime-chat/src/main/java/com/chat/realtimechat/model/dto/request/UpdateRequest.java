package com.chat.realtimechat.model.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateRequest {
    @Nullable
    private String username;

    @Nullable
    @Size(min = 8, message = "Password must have at least 8 characters")
    private String password;

    @Nullable
    @Email(message = "Invalid email address")
    private String email;

    @Nullable
    private String name;

    @Nullable
    private String surname;
}
