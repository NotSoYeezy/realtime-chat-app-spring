package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
}
