package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.validation.ValidEmail;
import com.chat.realtimechat.validation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class RegistrationRequest {
    @Null(message = "ID cannot be set by a user")
    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    private String name;
    private String surname;
}
