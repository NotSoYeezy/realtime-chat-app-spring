package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.enums.MessageContentType;
import lombok.Data;

@Data
public class ChatMessageRequest {
    private String content;
    private ChatMessage.MessageType type;
    private MessageContentType contentType;
    private Long groupId;
    private Long parentId;
}