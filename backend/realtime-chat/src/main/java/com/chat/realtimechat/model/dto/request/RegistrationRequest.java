package com.chat.realtimechat.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class RegistrationRequest {
    @Null(message = "ID cannot be set by a user")
    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must have at least 8 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    private String name;
    private String surname;
}
