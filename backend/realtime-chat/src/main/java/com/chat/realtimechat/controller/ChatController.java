package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.ChatMessageRequest;
import com.chat.realtimechat.model.dto.request.StatusUpdateRequest;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.ChatGroupRepository;
import com.chat.realtimechat.repository.ChatMessageRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ChatGroupRepository chatGroupRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        User sender = getCurrentUser(principal);

        ChatGroup group = chatGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(request.getContent());
        chatMessage.setSender(sender);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(request.getType());
        chatMessage.setGroup(group);

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        ChatMessageResponse response = ChatMessageResponse.fromEntity(saved);

        for (User member : group.getMembers()) {

            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/queue/messages",
                    response
            );
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageResponse addUser(Principal principal) {

        User user = getCurrentUser(principal);
        OnlineInfoResponse info = presenceService.getOnlineUsers().get(user.getUsername());

        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setSender(senderInfo(user));
        dto.setType(ChatMessage.MessageType.JOIN);
        dto.setUserStatus(info.getStatus());
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    @MessageMapping("/chat.typing/{groupId}")
    public void typing(@DestinationVariable Long groupId, Principal principal) {

        User user = getCurrentUser(principal);

        ChatMessageResponse notification = new ChatMessageResponse();
        notification.setSender(senderInfo(user));
        notification.setType(ChatMessage.MessageType.TYPING);
        notification.setTimestamp(LocalDateTime.now());
        notification.setGroupId(groupId);

        messagingTemplate.convertAndSend(
                "/topic/group." + groupId,
                notification
        );
    }

    @GetMapping("/api/chat/history/{groupId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long groupId) {
        List<ChatMessage> messages = chatMessageRepository.findByGroupId(groupId);

        return ResponseEntity.status(HttpStatus.OK).body(
                messages.stream()
                        .map(ChatMessageResponse::fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/api/chat/users/online")
    @ResponseBody
    public ResponseEntity<Map<String, OnlineInfoResponse>> getOnlineUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(presenceService.getOnlineUsers());
    }

    @MessageMapping("/user/setStatus")
    @SendTo("/topic/public")
    public ChatMessageResponse changeStatus(@Payload StatusUpdateRequest request, Principal principal) {

        User user = getCurrentUser(principal);
        presenceService.updateUserStatus(user.getUsername(), request.getStatus());

        ChatMessageResponse notification = new ChatMessageResponse();
        notification.setType(ChatMessage.MessageType.JOIN);
        notification.setSender(senderInfo(user));
        notification.setUserStatus(request.getStatus());
        notification.setTimestamp(LocalDateTime.now());

        return notification;
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(principal.getName()));
    }

    private OnlineInfoResponse senderInfo(User user) {
        return presenceService.getOnlineUsers()
                .getOrDefault(
                        user.getUsername(),
                        new OnlineInfoResponse(
                                user.getName(),
                                user.getSurname(),
                                user.getUsername(),
                                UserStatus.ONLINE
                        )
                );
    }
}
