package com.chat.realtimechat.service;

import com.chat.realtimechat.model.enums.UserStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPresenceServiceImpl implements UserPresenceService {
    private final Map<String, UserStatus> onlineUsers = new ConcurrentHashMap<>();

    @Override
    public void connectUser(String username) {
        onlineUsers.put(username, UserStatus.ONLINE);
    }

    @Override
    public void disconnectUser(String username) {
        onlineUsers.remove(username);
    }

    @Override
    public void updateUserStatus(String username, UserStatus status) {
        if (onlineUsers.containsKey(username)) {
            onlineUsers.put(username, status);
        }
    }

    @Override
    public Map<String, UserStatus> getOnlineUsers() {
        return  onlineUsers;
    }
}
