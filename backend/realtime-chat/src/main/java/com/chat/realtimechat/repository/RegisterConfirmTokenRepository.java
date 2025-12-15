package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.entity.auth.RegisterConfirmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterConfirmTokenRepository extends JpaRepository<RegisterConfirmToken, Long> {
    Optional<RegisterConfirmToken> findByToken(String token);
    void deleteByUser(User user);
}
