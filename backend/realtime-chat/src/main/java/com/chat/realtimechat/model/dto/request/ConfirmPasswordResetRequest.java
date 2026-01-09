package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.validation.ValidPassword;
import lombok.Data;

@Data
public class ConfirmPasswordResetRequest {
    private String token;
    @ValidPassword
    private String password;
}
