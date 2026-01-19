package com.chat.realtimechat.service.users;

import com.chat.realtimechat.model.dto.response.FriendInvitationResponse;
import com.chat.realtimechat.model.dto.response.FriendUserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface FriendshipService {
    void sendFriendRequest(Long senderId, Long receiverId);

    void acceptRequest(Long friendshipId, Long receiverId);

    void rejectRequest(Long friendshipId, Long receiverId);

    void blockUser(Long blockerId, Long blockedId);

    void removeFriend(Long userId, Long friendId);

    void cancelRequest(Long senderId, Long receiverId);

    void unblockUser(Long blockerId, Long blockedId);

    Page<FriendUserResponse> getFriends(Long friendId, Pageable pageable);

    Page<FriendInvitationResponse> getPendingRequests(Long userId, Pageable pageable);

    Page<FriendInvitationResponse> getOutgoingRequests(Long userId, Pageable pageable);

    Page<FriendUserResponse> getBlockedUsers(Long userId, Pageable pageable);

    Set<Long> getExcludedUserIds(Long userId);
}
