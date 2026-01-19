package com.chat.realtimechat.exception.auth;

public class UserNotConfirmedException extends RuntimeException {
    public UserNotConfirmedException(String message) {
        super(message);
    }
}
