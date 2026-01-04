package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineInfoResponse {
    private String name;
    private String surname;
    private String username;
    private UserStatus status;
}