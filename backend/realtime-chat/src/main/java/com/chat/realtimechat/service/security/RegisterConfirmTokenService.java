package com.chat.realtimechat.service.security;


import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.entity.auth.RegisterConfirmToken;

public interface RegisterConfirmTokenService {
    RegisterConfirmToken createRegisterConfirmToken(Long userId);
    void confirmUser(String token);
    User getUserByToken(String token);
    void sendConfirmationEmail(RegisterConfirmToken registerConfirmToken, String userEmail);
    void resendConfirmationToken(String email);
}
