package com.chat.realtimechat.exception.google;

public class AccountNotConnectedException extends RuntimeException {
    public AccountNotConnectedException() {
        super("Account not connected");
    }
}
