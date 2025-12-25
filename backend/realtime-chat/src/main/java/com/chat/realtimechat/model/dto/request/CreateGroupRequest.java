package com.chat.realtimechat.model.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class CreateGroupRequest {
    private String name;
    private Set<Long> memberIds;
}