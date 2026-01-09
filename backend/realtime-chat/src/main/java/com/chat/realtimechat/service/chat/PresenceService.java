package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.enums.UserStatus;

public interface PresenceService {

    void connectUser(String username);

    void disconnectUser(String username);

    void heartbeat(String username);

    void setUserStatus(String username, UserStatus status);

    UserStatus getResolvedStatus(String username);
}