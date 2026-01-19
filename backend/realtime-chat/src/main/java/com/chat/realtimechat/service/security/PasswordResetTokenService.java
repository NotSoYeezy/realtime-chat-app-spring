package com.chat.realtimechat.service.security;

import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.entity.auth.PasswordResetToken;

public interface PasswordResetTokenService {

    PasswordResetToken createPasswordResetToken(User user);

    void initiatePasswordReset(String email);
}
