package com.chat.realtimechat.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("user with username " + username + " already exists");
    }
}
