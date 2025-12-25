package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.BlockUserRequest;
import com.chat.realtimechat.model.dto.request.FriendInvitationRequest;
import com.chat.realtimechat.model.dto.response.FriendInvitationResponse;
import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.service.FriendshipService;
import com.chat.realtimechat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    private Long getCurrentUserId(UserDetails user) {
        User u = userService.findUsersByUsername(user.getUsername())
                // TODO: custom exception
                .orElseThrow(() -> new RuntimeException("User not found"));
        return u.getId();
    }

    @PostMapping("/requests")
    public ResponseEntity<?> sendRequest(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody FriendInvitationRequest request
    ) {
        Long senderId = getCurrentUserId(user);

        friendshipService.sendFriendRequest(
                senderId,
                request.getReceiverId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body("Friend request sent.");
    }

    @PatchMapping("/requests/{id}/accept")
    public ResponseEntity<?> acceptRequest(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id
    ) {
        Long receiverId = getCurrentUserId(user);

        friendshipService.acceptRequest(id, receiverId);

        return ResponseEntity.status(HttpStatus.OK).body("Friend request accepted");
    }

    @PatchMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id
    ) {
        Long receiverId = getCurrentUserId(user);

        friendshipService.rejectRequest(id, receiverId);

        return ResponseEntity.status(HttpStatus.OK).body("Friend request rejected");
    }

    @PostMapping("/blocks")
    public ResponseEntity<?> blockUser(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody BlockUserRequest request
    ) {
        Long blockerId = getCurrentUserId(user);

        friendshipService.blockUser(
                blockerId,
                request.getBlockedUserId()
        );

        return ResponseEntity.status(HttpStatus.OK).body("User blocked");
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> removeFriend(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long friendId
    ) {
        Long userId = getCurrentUserId(user);

        friendshipService.removeFriend(userId, friendId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<FriendUserResponse>> getFriends(
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = getCurrentUserId(user);

        return ResponseEntity.status(HttpStatus.OK).body(
                friendshipService.getFriends(userId)
        );
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendInvitationResponse>> getIncoming(
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = getCurrentUserId(user);

        return ResponseEntity.status(HttpStatus.OK).body(
                friendshipService.getPendingRequests(userId)
        );
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendInvitationResponse>> getOutgoing(
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = getCurrentUserId(user);

        return ResponseEntity.status(HttpStatus.OK).body(
                friendshipService.getOutgoingRequests(userId)
        );
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<FriendUserResponse>> getBlocked(
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = getCurrentUserId(user);

        return ResponseEntity.status(HttpStatus.OK).body(
                friendshipService.getBlockedUsers(userId)
        );
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<?> cancelRequest(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id
    ) {
        Long senderId = getCurrentUserId(user);

        friendshipService.cancelRequest(id, senderId);

        return ResponseEntity.status(HttpStatus.OK).body("Friend request canceled");
    }

    @DeleteMapping("/blocks/{blockedUserId}")
    public ResponseEntity<?> unblock(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long blockedUserId
    ) {
        Long userId = getCurrentUserId(user);
        friendshipService.unblockUser(userId, blockedUserId);
        return ResponseEntity.noContent().build();
    }

}
