package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private ChatMessage.MessageType type;
    private UserStatus userStatus;

    public static ChatMessageResponse fromEntity(ChatMessage message) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setType(message.getType());

        if (message.getSender() != null) {
            dto.setSender(message.getSender().getUsername());
        }
        return dto;
    }
}