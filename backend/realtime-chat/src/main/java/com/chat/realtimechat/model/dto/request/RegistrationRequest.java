package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.validation.ValidEmail;
import com.chat.realtimechat.validation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class RegistrationRequest {
    @Null(message = "ID cannot be set by a user")
    private Long id;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;
}
