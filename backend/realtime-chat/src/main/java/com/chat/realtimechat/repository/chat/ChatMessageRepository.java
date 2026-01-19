package com.chat.realtimechat.repository.chat;

import com.chat.realtimechat.model.entity.chat.ChatMessage;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.enums.MessageContentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Slice<ChatMessage> findByGroupId(Long groupId, Pageable pageable);

    Optional<ChatMessage> findFirstByGroupIdOrderByTimestampDesc(Long groupId);
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.group.id = :groupId " +
            "AND m.timestamp > :lastReadTime " +
            "AND m.sender.id <> :currentUserId")
    long countUnreadMessages(@Param("groupId") Long groupId,
                             @Param("currentUserId") Long currentUserId,
                             @Param("lastReadTime") LocalDateTime lastReadTime);
    List<ChatMessage> findByGroupIdAndContentType(Long groupId, MessageContentType contentType);
    void deleteAllBySender(User sender);
}