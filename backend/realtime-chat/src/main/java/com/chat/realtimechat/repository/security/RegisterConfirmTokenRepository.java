package com.chat.realtimechat.repository.security;

import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.entity.auth.RegisterConfirmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterConfirmTokenRepository extends JpaRepository<RegisterConfirmToken, Long> {
    Optional<RegisterConfirmToken> findByToken(String token);
    void deleteByUser(User user);
}
