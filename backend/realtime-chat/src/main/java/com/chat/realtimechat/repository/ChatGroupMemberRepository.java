package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.ChatGroupMember;
import com.chat.realtimechat.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGroupMemberRepository extends JpaRepository<ChatGroupMember, Long> {
    void deleteByUser(User user);
}
