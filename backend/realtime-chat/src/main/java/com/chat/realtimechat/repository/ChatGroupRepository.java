package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findAllByMembersContaining(User user);
}
