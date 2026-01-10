package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.enums.MessageContentType;
import com.chat.realtimechat.model.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private Long id;
    private OnlineInfoResponse sender;
    private String content;
    private Long groupId;
    private String groupName;
    private LocalDateTime timestamp;
    private ChatMessage.MessageType type;
    private MessageContentType contentType;
    private UserStatus userStatus;
    private ChatMessageResponse parent;

    public static ChatMessageResponse fromEntity(ChatMessage message) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setContentType(message.getContentType());
        dto.setTimestamp(message.getTimestamp());
        dto.setType(message.getType());

        if (message.getSender() != null) {
            dto.setSender(new OnlineInfoResponse(
                    message.getSender().getName(),
                    message.getSender().getSurname(),
                    message.getSender().getUsername(),
                    UserStatus.ONLINE
            ));
        }

        if (message.getGroup() != null) {
            dto.setGroupId(message.getGroup().getId());
            dto.setGroupName(message.getGroup().getName());
        }

        if (message.getParent() != null) {
            ChatMessageResponse parentDto = getChatMessageResponseParent(message);
            dto.setParent(parentDto);
        }

        return dto;
    }

    private static ChatMessageResponse getChatMessageResponseParent(ChatMessage message) {
        ChatMessageResponse parentDto = new ChatMessageResponse();
        parentDto.setContent(message.getParent().getContent());
        parentDto.setType(message.getParent().getType());
        parentDto.setContentType(message.getParent().getContentType());
        if (message.getParent().getSender() != null) {
            parentDto.setSender(new OnlineInfoResponse(
                    message.getParent().getSender().getName(),
                    message.getParent().getSender().getSurname(),
                    message.getParent().getSender().getUsername(),
                    UserStatus.ONLINE
            ));
        }
        return parentDto;
    }
}