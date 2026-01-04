package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.ChatGroupMember;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatGroupMemberResponse {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private LocalDateTime lastReadTime;

    public static ChatGroupMemberResponse fromEntity(ChatGroupMember member) {
        ChatGroupMemberResponse response = new ChatGroupMemberResponse();
        response.setId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setName(member.getUser().getName());
        response.setSurname(member.getUser().getSurname());
        response.setLastReadTime(member.getLastReadTime());
        return response;
    }
}