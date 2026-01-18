package com.chat.realtimechat.service;

import com.chat.realtimechat.exception.friendship.*;
import com.chat.realtimechat.model.dto.response.FriendInvitationResponse;
import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.entity.Friendship;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.FriendshipStatus;
import com.chat.realtimechat.repository.FriendshipRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;

    @Override
    public void sendFriendRequest(Long senderId, Long targetId) {

        validateNotSelf(senderId, targetId);

        User sender = getUser(senderId);
        User target = getUser(targetId);

        Friendship direct = findRelation(senderId, targetId);
        Friendship reverse = findRelation(targetId, senderId);

        validateNoBlock(direct, reverse);
        validateNoFriendship(direct, reverse);
        validateNoPendingRequest(direct, reverse);

        if (wasRejected(direct)) {
            friendshipRepository.delete(direct);
            createPending(sender, target);
            return;
        }

        if (wasRejected(reverse)) {
            friendshipRepository.delete(reverse);
            createPending(sender, target);
            return;
        }

        createPending(sender, target);
    }

    @Override
    public void acceptRequest(Long requestId, Long receiverId) {

        Friendship request = getFriendship(requestId);

        validateReceiver(request, receiverId);
        validatePendingRequest(request);

        request.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(request);

        groupService.createPrivateGroup(request.getUser(), request.getFriend());
    }

    @Override
    public void rejectRequest(Long requestId, Long receiverId) {

        Friendship request = getFriendship(requestId);

        validateReceiver(request, receiverId);
        validatePendingRequest(request);

        request.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(request);
    }

    @Override
    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        System.out.println(">>>METHOD: blockUser<<<");

        validateNotSelf(blockerId, blockedId);

        User blocker = getUser(blockerId);
        User blocked = getUser(blockedId);

        Friendship direct = findRelation(blockerId, blockedId);
        Friendship reverse = findRelation(blockedId, blockerId);

        if (reverse != null && reverse.getStatus() == FriendshipStatus.BLOCKED) {
            return;
        }

        if (reverse != null) {
            friendshipRepository.delete(reverse);
        }

        if (direct != null) {
            direct.setStatus(FriendshipStatus.BLOCKED);
            return;
        }

        Friendship block = new Friendship();
        block.setUser(blocker);
        block.setFriend(blocked);
        block.setStatus(FriendshipStatus.BLOCKED);

        friendshipRepository.save(block);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {

        validateNotSelf(userId, friendId);

        Friendship direct = findRelation(userId, friendId);
        Friendship reverse = findRelation(friendId, userId);

        if (direct != null && direct.getStatus() == FriendshipStatus.ACCEPTED)
            friendshipRepository.delete(direct);

        if (reverse != null && reverse.getStatus() == FriendshipStatus.ACCEPTED)
            friendshipRepository.delete(reverse);
    }

    @Override
    public void cancelRequest(Long friendshipId, Long senderId) {

        Friendship request = getFriendship(friendshipId);

        if (request.getStatus() == FriendshipStatus.ACCEPTED) {
            // TODO: change later if needed
            return; // This means sender canceled invite after acceptance, this can be later changed
        }

        if (!request.getUser().getId().equals(senderId))
            throw new FriendshipOperationException("Invalid sender");

        validatePendingRequest(request);

        friendshipRepository.delete(request);
    }

    @Override
    public void unblockUser(Long blockerId, Long blockedId) {

        validateNotSelf(blockerId, blockedId);

        Friendship direct = findRelation(blockerId, blockedId);

        if (direct == null || direct.getStatus() != FriendshipStatus.BLOCKED)
            throw new FriendshipOperationException("No blocked relationship found");

        friendshipRepository.delete(direct);
    }

    @Override
    public Page<FriendUserResponse> getFriends(Long userId, Pageable pageable) {
        return friendshipRepository.findAllByUserIdOrFriendIdAndStatus(userId, FriendshipStatus.ACCEPTED, pageable)
                .map(f -> {
                    if (f.getUser().getId().equals(userId)) {
                        return toUserResponse(f.getFriend());
                    } else {
                        return toUserResponse(f.getUser());
                    }
                });
    }

    @Override
    public Page<FriendInvitationResponse> getPendingRequests(Long userId, Pageable pageable) {
        return friendshipRepository
                .findAllByFriendIdAndStatus(userId, FriendshipStatus.PENDING, pageable)
                .map(f -> toFriendRequestResponse(f, false));
    }

    @Override
    public Page<FriendInvitationResponse> getOutgoingRequests(Long userId, Pageable pageable) {
        return friendshipRepository
                .findAllByUserIdAndStatus(userId, FriendshipStatus.PENDING, pageable)
                .map(f -> toFriendRequestResponse(f, true));
    }

    @Override
    public Page<FriendUserResponse> getBlockedUsers(Long userId, Pageable pageable) {
        return friendshipRepository
                .findAllByUserIdAndStatus(userId, FriendshipStatus.BLOCKED, pageable)
                .map(f -> toUserResponse(f.getFriend()));
    }

    @Override
    public Set<Long> getExcludedUserIds(Long userId) {

        Set<Long> excluded = new HashSet<>();
        excluded.add(userId);

        List<Friendship> friendsAsUser = friendshipRepository.findAllByUserIdAndStatus(
                userId, FriendshipStatus.ACCEPTED);

        List<Friendship> friendsAsFriend = friendshipRepository.findAllByFriendIdAndStatus(
                userId, FriendshipStatus.ACCEPTED);

        friendsAsUser.forEach(f -> excluded.add(f.getFriend().getId()));
        friendsAsFriend.forEach(f -> excluded.add(f.getUser().getId()));

        List<Friendship> outgoing = friendshipRepository.findAllByUserIdAndStatus(
                userId, FriendshipStatus.PENDING);

        outgoing.forEach(f -> excluded.add(f.getFriend().getId()));

        List<Friendship> incoming = friendshipRepository.findAllByFriendIdAndStatus(
                userId, FriendshipStatus.PENDING);

        incoming.forEach(f -> excluded.add(f.getUser().getId()));

        List<Friendship> blockedAsUser = friendshipRepository.findAllByUserIdAndStatus(
                userId, FriendshipStatus.BLOCKED);

        List<Friendship> blockedAsFriend = friendshipRepository.findAllByFriendIdAndStatus(
                userId, FriendshipStatus.BLOCKED);

        blockedAsUser.forEach(f -> excluded.add(f.getFriend().getId()));
        blockedAsFriend.forEach(f -> excluded.add(f.getUser().getId()));

        return excluded;
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    private Friendship getFriendship(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new FriendshipOperationException("Friendship not found"));
    }

    private Friendship findRelation(Long userId, Long friendId) {
        return friendshipRepository.findByUserIdAndFriendId(userId, friendId).orElse(null);
    }

    private void validateNotSelf(Long a, Long b) {
        if (a.equals(b))
            throw new CannotFriendYourselfException();
    }

    private void validateReceiver(Friendship request, Long receiverId) {
        if (!request.getFriend().getId().equals(receiverId))
            throw new FriendshipOperationException("Invalid receiver");
    }

    private void validatePendingRequest(Friendship request) {
        if (request.getStatus() != FriendshipStatus.PENDING)
            throw new PendingFriendRequestException();
    }

    private void validateNoBlock(Friendship aToB, Friendship bToA) {
        if ((aToB != null && aToB.getStatus() == FriendshipStatus.BLOCKED) ||
                (bToA != null && bToA.getStatus() == FriendshipStatus.BLOCKED))
            throw new BlockedRelationshipException();
    }

    private void validateNoFriendship(Friendship aToB, Friendship bToA) {
        if ((aToB != null && aToB.getStatus() == FriendshipStatus.ACCEPTED) ||
                (bToA != null && bToA.getStatus() == FriendshipStatus.ACCEPTED))
            throw new FriendshipAlreadyExistsException();
    }

    private void validateNoPendingRequest(Friendship aToB, Friendship bToA) {
        if ((aToB != null && aToB.getStatus() == FriendshipStatus.PENDING) ||
                (bToA != null && bToA.getStatus() == FriendshipStatus.PENDING))
            throw new PendingFriendRequestException();
    }

    private boolean wasRejected(Friendship relation) {
        return relation != null && relation.getStatus() == FriendshipStatus.REJECTED;
    }

    private void createPending(User sender, User receiver) {
        Friendship f = new Friendship();
        f.setUser(sender);
        f.setFriend(receiver);
        f.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(f);
    }

    private FriendUserResponse toUserResponse(User user) {
        return new FriendUserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname());
    }

    private FriendInvitationResponse toFriendRequestResponse(Friendship f, boolean outgoing) {
        FriendUserResponse sender = toUserResponse(f.getUser());
        FriendUserResponse receiver = toUserResponse(f.getFriend());
        return new FriendInvitationResponse(
                f.getId(),
                sender,
                receiver,
                f.getStatus(),
                outgoing);
    }
}
