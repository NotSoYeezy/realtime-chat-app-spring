package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.dto.request.UpdateGroupRequest;
import com.chat.realtimechat.model.dto.response.ChatGroupResponse;
import com.chat.realtimechat.model.entity.chat.ChatGroup;
import com.chat.realtimechat.model.entity.users.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface GroupService {
    ChatGroup createGroup(String creatorUsername, String groupName, Set<Long> memberIds, MultipartFile image);

    ChatGroup createPrivateGroup(User user1, User user2);

    Page<ChatGroupResponse> getUserGroupsResponse(String username, Pageable pageable);

    ChatGroupResponse getGroupInfo(Long groupId, String username);

    ChatGroup updateGroup(Long groupId, UpdateGroupRequest request, String username, MultipartFile image);

    void addMembers(Long groupId, Set<Long> memberIds, String username);

    void removeMembers(Long groupId, Set<Long> memberIds, String username);

    void addAdmin(Long groupId, Long userId, String requestorUsername);

    void removeAdmin(Long groupId, Long userId, String requestorUsername);

    void markAsRead(Long groupId, String username);

    void muteGroup(Long groupId, String username);

    void unmuteGroup(Long groupId, String username);

    void setColorTheme(Long groupId, String newColorTheme);

    String getColorTheme(Long groupId);
}
