package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByGroupIdOrderByTimestampAsc(Long groupId);
    Optional<ChatMessage> findFirstByGroupIdOrderByTimestampDesc(Long groupId);
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.group.id = :groupId " +
            "AND m.timestamp > :lastReadTime " +
            "AND m.sender.id <> :currentUserId")
    long countUnreadMessages(@Param("groupId") Long groupId,
                             @Param("currentUserId") Long currentUserId,
                             @Param("lastReadTime") LocalDateTime lastReadTime);
}