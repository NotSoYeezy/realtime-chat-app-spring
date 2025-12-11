package com.chat.realtimechat.websocket.listener;

import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketListener {

    private final UserPresenceService presenceService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            String username = user.getName();
            presenceService.connectUser(username);

            UserStatus currentStatus = presenceService.getOnlineUsers()
                    .get(username)
                    .getStatus();

            sendPresenceMessage(username, ChatMessage.MessageType.JOIN, currentStatus);
        }
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor =  StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        if (user != null) {
            String username = user.getName();

            presenceService.disconnectUser(username);

            userRepository.findByUsername(username).ifPresent(u -> {
                u.setLastSeen(LocalDateTime.now());
                userRepository.save(u);
            });
            sendPresenceMessage(username, ChatMessage.MessageType.LEAVE, UserStatus.OFFLINE);
        }
    }

    private void sendPresenceMessage(String username, ChatMessage.MessageType type,  UserStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        ChatMessageResponse message = new ChatMessageResponse();
        message.setSender(new OnlineInfoResponse(
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                status
        ));
        message.setType(type);
        message.setTimestamp(LocalDateTime.now());
        message.setUserStatus(status);
        message.setContent(type == ChatMessage.MessageType.JOIN ? "joined chat" : "left chat");
        messagingTemplate.convertAndSend("/topic/public", message);
    }

}
