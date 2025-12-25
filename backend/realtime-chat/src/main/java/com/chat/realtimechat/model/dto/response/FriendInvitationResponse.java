package com.chat.realtimechat.model.dto.response;

import com.chat.realtimechat.model.enums.FriendshipStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FriendInvitationResponse {
    private final Long friendshipId;
    private final FriendUserResponse fromUser;
    private final FriendUserResponse toUser;
    private final FriendshipStatus status;
    private final boolean direction;
}
