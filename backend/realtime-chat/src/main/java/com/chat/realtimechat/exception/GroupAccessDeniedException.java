package com.chat.realtimechat.exception;

public class GroupAccessDeniedException extends RuntimeException {
    public GroupAccessDeniedException(String message) {
        super(message);
    }
}
