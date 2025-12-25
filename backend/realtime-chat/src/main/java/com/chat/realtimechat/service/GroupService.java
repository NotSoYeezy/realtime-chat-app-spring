package com.chat.realtimechat.service;

import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.User;

import java.util.List;
import java.util.Set;

public interface GroupService {
    ChatGroup createGroup(User creator, Set<Long> memberIds);
    ChatGroup createPrivateGroup(User user1, User user2);
    List<ChatGroup> getUserGroups(User user);

}
