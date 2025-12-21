package com.chat.realtimechat.service.notifiers;

import org.springframework.stereotype.Service;

@Service
public interface UserEmailNotifier {
    void sendMessage(String message, String topic, String to);
}
