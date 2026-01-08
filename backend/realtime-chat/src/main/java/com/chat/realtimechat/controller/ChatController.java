package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.ChatMessageRequest;
import com.chat.realtimechat.model.dto.request.StatusUpdateRequest;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.enums.MessageContentType;
import com.chat.realtimechat.repository.ChatMessageRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.chat.ChatService;
import com.chat.realtimechat.service.chat.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final UserPresenceService presenceService;
    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;

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

    @GetMapping("/api/chat/users/online")
    @ResponseBody
    public ResponseEntity<Map<String, OnlineInfoResponse>> getOnlineUsers(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(presenceService.getOnlineUsers(principal.getName()));
    }

    @MessageMapping("/user/setStatus")
    public void changeStatus(@Payload StatusUpdateRequest request, Principal principal) {
        chatService.handleStatusChange(
                request,
                principal
        );
    }

    @GetMapping("api/messages/group/{groupId}/images")
    @ResponseBody
    public ResponseEntity<List<String>> getGroupMedia(@PathVariable Long groupId) {

        List<ChatMessage> mediaMessages = chatMessageRepository.findByGroupIdAndContentType(groupId, MessageContentType.IMAGE);

        List<String> imageUrls = mediaMessages.stream()
                .map(ChatMessage::getContent)
                .collect(Collectors.toList());

        return ResponseEntity.ok(imageUrls);
    }
}
