package com.chat.realtimechat.exception.google;

public class NoAuthenticationException extends RuntimeException {
    public NoAuthenticationException() {
        super("No authentication scopes found");
    }
}
