package com.chat.realtimechat.service;

import com.chat.realtimechat.model.dto.response.FriendInvitationResponse;
import com.chat.realtimechat.model.dto.response.FriendUserResponse;

import java.util.List;
import java.util.Set;

public interface FriendshipService
{
    void sendFriendRequest(Long senderId, Long receiverId);

    void acceptRequest(Long friendshipId, Long receiverId);

    void rejectRequest(Long friendshipId, Long receiverId);

    void blockUser(Long blockerId, Long blockedId);

    void removeFriend(Long userId, Long friendId);

    void cancelRequest(Long senderId, Long receiverId);

    void unblockUser(Long blockerId, Long blockedId);

    List<FriendUserResponse> getFriends(Long friendId);

    List<FriendInvitationResponse> getPendingRequests(Long userId);

    List<FriendInvitationResponse> getOutgoingRequests(Long userId);

    List<FriendUserResponse> getBlockedUsers(Long userId);

    Set<Long> getExcludedUserIds(Long userId);
}
