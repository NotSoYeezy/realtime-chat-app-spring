package com.chat.realtimechat.repository.chat;

import com.chat.realtimechat.model.entity.chat.ChatGroup;
import com.chat.realtimechat.model.entity.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
        @Query("SELECT gm.group FROM ChatGroupMember gm WHERE gm.user = :user AND gm.group.isDeleted = false")
        Page<ChatGroup> findUserGroups(@Param("user") User user, Pageable pageable);

        @Query("SELECT g FROM ChatGroup g " +
                        "LEFT JOIN FETCH g.members m " +
                        "LEFT JOIN FETCH m.user " +
                        "WHERE g.id = :id AND g.isDeleted = false")
        Optional<ChatGroup> findByIdWithMembers(@Param("id") Long id);

        @Query("SELECT g FROM ChatGroup g WHERE g.type = 'PRIVATE' " +
                        "AND g.isDeleted = false " +
                        "AND EXISTS (SELECT m1 FROM g.members m1 WHERE m1.user = :user1) " +
                        "AND EXISTS (SELECT m2 FROM g.members m2 WHERE m2.user = :user2)")
        Optional<ChatGroup> findPrivateGroup(@Param("user1") User user1, @Param("user2") User user2);

        ChatGroup getById(Long id);
}
