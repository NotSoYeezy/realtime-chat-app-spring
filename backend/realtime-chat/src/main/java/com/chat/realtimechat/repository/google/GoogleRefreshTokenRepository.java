package com.chat.realtimechat.repository.google;

import com.chat.realtimechat.model.entity.auth.GoogleRefreshToken;
import com.chat.realtimechat.model.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleRefreshTokenRepository extends JpaRepository<GoogleRefreshToken, Long> {
    Optional<GoogleRefreshToken> findByUserId(Long userId);
    void deleteByUser(User user);
}
