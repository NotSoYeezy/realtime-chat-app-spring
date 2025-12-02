package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.model.enums.UserStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private UserStatus status;
}
