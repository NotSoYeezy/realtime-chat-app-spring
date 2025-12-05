package com.chat.realtimechat.exception;

public class IncorrectRefreshTokenException extends RuntimeException {
    public IncorrectRefreshTokenException(String message) {
        super(message);
    }
}
