package com.chat.realtimechat.model.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class GroupMembersRequest {
    private Set<Long> userIds;
}
