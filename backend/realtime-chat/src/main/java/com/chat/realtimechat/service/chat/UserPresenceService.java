package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.enums.UserStatus;

import java.util.Map;

public interface UserPresenceService {
    void connectUser(String username);
    void disconnectUser(String username);
    void updateUserStatus(String username, UserStatus status);
    void heartbeat(String username);
    Map<String, OnlineInfoResponse> getOnlineUsers(String username);
    OnlineInfoResponse getOnlineUser(String username);
}