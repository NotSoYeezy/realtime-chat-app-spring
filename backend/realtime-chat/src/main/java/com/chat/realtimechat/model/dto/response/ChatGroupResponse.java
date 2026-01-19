package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.config.UploadUrlConfig;
import com.chat.realtimechat.model.entity.chat.ChatGroup;
import com.chat.realtimechat.model.enums.GroupType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ChatGroupResponse {
    private Long id;
    private String name;
    private int unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
    private GroupType groupType;
    private String imageUrl;
    private boolean muted;

    private Set<UserResponse> admins;
    private Set<ChatGroupMemberResponse> members;

    public static ChatGroupResponse fromEntity(ChatGroup group) {
        ChatGroupResponse response = new ChatGroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setGroupType(group.getType());
        response.setCreatedAt(group.getCreatedAt());
        response.setImageUrl(group.getImageUrl());
        response.setUnreadCount(0);

        if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
            response.setImageUrl(UploadUrlConfig.getUploadUrl() + group.getImageUrl());
        }

        if (group.getAdmins() != null) {
            response.setAdmins(group.getAdmins().stream()
                    .map(UserResponse::fromEntity)
                    .collect(Collectors.toSet())
            );
        }

        if (group.getMembers() != null) {
            response.setMembers(group.getMembers().stream()
                    .map(ChatGroupMemberResponse::fromEntity)
                    .collect(Collectors.toSet()));
        }

        return response;
    }
}