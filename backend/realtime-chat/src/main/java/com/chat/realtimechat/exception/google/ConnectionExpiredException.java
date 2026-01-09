package com.chat.realtimechat.exception.google;

public class ConnectionExpiredException extends RuntimeException {
    public ConnectionExpiredException() {
        super("Google Calendar connection expired");
    }
}
