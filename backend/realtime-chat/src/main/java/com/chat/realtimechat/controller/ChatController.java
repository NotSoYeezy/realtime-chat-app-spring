package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.ChatMessageRequest;
import com.chat.realtimechat.model.dto.request.StatusUpdateRequest;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.ChatMessageRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final UserPresenceService presenceService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageResponse sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            // TODO: CHANGE ERROR TYPE
            throw new RuntimeException("Unauthorized");
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(request.getContent());
        chatMessage.setSender(user);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(request.getType());

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        return ChatMessageResponse.fromEntity(saved);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageResponse addUser(Principal principal) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setSender(principal.getName());
        dto.setType(ChatMessage.MessageType.JOIN);
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    @MessageMapping("/chat.typing")
    @SendTo("/topic/public")
    public ChatMessageResponse typing(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        ChatMessageResponse notification = new ChatMessageResponse();
        notification.setSender(principal.getName());
        notification.setType(ChatMessage.MessageType.TYPING);
        notification.setTimestamp(LocalDateTime.now());

        return notification;
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory() {
        return ResponseEntity.ok(
                chatMessageRepository.findAll().stream()
                        .map(ChatMessageResponse::fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/api/chat/users/online")
    @ResponseBody
    public ResponseEntity<Map<String, UserStatus>> getOnlineUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(presenceService.getOnlineUsers());
    }

    @MessageMapping("/user/setStatus")
    @SendTo("/topic/public")
    public ChatMessageResponse changeStatus(@Payload StatusUpdateRequest request, Principal principal) {
        String username = principal.getName();

        presenceService.updateUserStatus(username, request.getStatus());

        ChatMessageResponse notification = new ChatMessageResponse();
        notification.setType(ChatMessage.MessageType.JOIN);
        notification.setSender(username);
        notification.setUserStatus(request.getStatus());
        notification.setTimestamp(LocalDateTime.now());

        return notification;
    }
}
