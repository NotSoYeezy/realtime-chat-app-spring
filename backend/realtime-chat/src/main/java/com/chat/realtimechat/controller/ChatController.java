package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.ChatMessageRequest;
import com.chat.realtimechat.model.dto.request.StatusUpdateRequest;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.service.chat.ChatService;
import com.chat.realtimechat.service.chat.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final PresenceService presenceService;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        chatService.sendMessage(
                request.getGroupId(),
                request,
                principal
        );
    }

    @MessageMapping("/chat.typing/{groupId}")
    public void typing(@DestinationVariable Long groupId, Principal principal) {
        chatService.handleTyping(
                groupId,
                principal
        );
    }

    @GetMapping("/api/chat/history/{groupId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long groupId, Principal principal) {
        return chatService.loadGroupHistory(
                groupId,
                principal
        );
    }

    @PostMapping("/api/chat/heartbeat")
    @ResponseBody
    public ResponseEntity<Void> heartbeat(Principal principal) {
        presenceService.heartbeat(principal.getName());
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/user/setStatus")
    public void changeStatus(@Payload StatusUpdateRequest request, Principal principal) {
        chatService.handleStatusChange(
                request,
                principal
        );
    }
}
