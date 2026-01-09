package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.FriendshipService;
import com.chat.realtimechat.service.chat.PresenceService;
import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PresenceController {

    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private final PresenceService presenceService;

    @GetMapping("/api/presence/friends")
    public Map<String, UserStatus> getFriendsPresence(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        return friendshipService.getFriendsUsernames(user.getId()).stream()
                .collect(Collectors.toMap(
                        u -> u,
                        presenceService::getResolvedStatus
                ));
    }
}
