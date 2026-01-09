package com.chat.realtimechat.service.notifiers;

import com.chat.realtimechat.model.dto.response.FriendPresenceEvent;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.FriendshipService;
import com.chat.realtimechat.service.chat.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresenceServiceNotifierImpl implements PresenceServiceNotifier {

    private final SimpMessagingTemplate messagingTemplate;
    private final FriendshipService friendshipService;
    private final PresenceService presenceService;
    private final UserRepository userRepository;

    @Override
    public void notifyFriends(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        UserStatus status = presenceService.getResolvedStatus(username);
        FriendPresenceEvent event = new FriendPresenceEvent(username, status);

        Set<String> friends = friendshipService.getFriendsUsernames(user.getId());

        for (String friendUsername : friends) {
            messagingTemplate.convertAndSendToUser(
                    friendUsername,
                    "/queue/presence",
                    event
            );
        }
    }
}

