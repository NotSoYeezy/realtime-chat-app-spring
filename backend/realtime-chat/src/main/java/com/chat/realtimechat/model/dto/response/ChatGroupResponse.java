package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.ChatMessage;
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
    private GroupType groupType;
    private Set<UserResponse> members;

    public static ChatGroupResponse fromEntity(ChatGroup group) {
        ChatGroupResponse response = new ChatGroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setGroupType(group.getType());
        // TODO: FIGURE OUT HOW TO DEAL WITH THAT READ COUNT
        response.setUnreadCount(0);

        if (group.getMembers() != null) {
            response.setMembers(group.getMembers().stream()
                    .map(user -> new UserResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getUsername(),
                            user.getName(),
                            user.getSurname()
                    ))
                    .collect(Collectors.toSet()));
        }

        return response;
    }
}