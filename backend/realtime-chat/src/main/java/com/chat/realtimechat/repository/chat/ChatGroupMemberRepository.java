package com.chat.realtimechat.repository.chat;

import com.chat.realtimechat.model.entity.chat.ChatGroupMember;
import com.chat.realtimechat.model.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGroupMemberRepository extends JpaRepository<ChatGroupMember, Long> {
    void deleteByUser(User user);
}
