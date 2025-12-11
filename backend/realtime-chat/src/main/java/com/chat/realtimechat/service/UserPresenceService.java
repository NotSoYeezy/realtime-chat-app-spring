package com.chat.realtimechat.service;

import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.enums.UserStatus;

import java.util.Map;

public interface UserPresenceService {
    void connectUser(String username);
    void disconnectUser(String username);
    void updateUserStatus(String username, UserStatus status);
    Map<String, OnlineInfoResponse> getOnlineUsers();
}