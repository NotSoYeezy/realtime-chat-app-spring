package com.chat.realtimechat.websocket.listener;

import com.chat.realtimechat.model.dto.response.FriendPresenceEvent;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.chat.PresenceService;
import com.chat.realtimechat.service.FriendshipService;
import com.chat.realtimechat.service.notifiers.PresenceServiceNotifier;
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
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketListener {

    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final PresenceServiceNotifier presenceServiceNotifier;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String username = principal.getName();
        presenceService.connectUser(username);
        presenceServiceNotifier.notifyFriends(username);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal principal = StompHeaderAccessor.wrap(event.getMessage()).getUser();
        if (principal == null) return;

        String username = principal.getName();
        presenceService.disconnectUser(username);

        userRepository.findByUsername(username).ifPresent(u -> {
            u.setLastSeen(LocalDateTime.now());
            userRepository.save(u);
        });
        presenceServiceNotifier.notifyFriends(username);
    }
}
