package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.dto.request.ChatMessageRequest;
import com.chat.realtimechat.model.dto.request.StatusUpdateRequest;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

public interface ChatService {
    void sendMessage(Long groupId, ChatMessageRequest request, Principal principal);
    void handleTyping(Long groupId, Principal principal);
    ResponseEntity<List<ChatMessageResponse>> loadGroupHistory(Long groupId, Principal principal, int page, int size);
    void handleStatusChange(StatusUpdateRequest request, Principal principal);
}
