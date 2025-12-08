package com.chat.realtimechat.model.dto.request;

import com.chat.realtimechat.model.entity.ChatMessage;
import lombok.Data;

@Data
public class ChatMessageRequest {
    private String content;
    private ChatMessage.MessageType type;
    private String roomId;
}