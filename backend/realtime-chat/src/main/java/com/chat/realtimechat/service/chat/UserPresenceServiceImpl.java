package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserPresenceServiceImpl implements UserPresenceService {
    
    private final UserRepository userRepository;
    private final Map<String, OnlineInfoResponse> onlineUsers = new ConcurrentHashMap<>();

    @Override
    public void connectUser(String username) {
        if (onlineUsers.containsKey(username)) {
            return;
        }

        userRepository.findByUsername(username).ifPresent(user ->
                onlineUsers.put(username, new OnlineInfoResponse(
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                UserStatus.ONLINE
        )));
    }

    @Override
    public void disconnectUser(String username) {
        onlineUsers.remove(username);
    }

    @Override
    public void updateUserStatus(String username, UserStatus status) {
        if (onlineUsers.containsKey(username)) {
            onlineUsers.get(username).setStatus(status);
        }
    }

    @Override
    public Map<String, OnlineInfoResponse> getOnlineUsers() {
        return onlineUsers;
    }
}
