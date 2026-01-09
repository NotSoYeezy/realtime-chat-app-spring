package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.validation.ValidEmail;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @ValidEmail
    private String email;
}
