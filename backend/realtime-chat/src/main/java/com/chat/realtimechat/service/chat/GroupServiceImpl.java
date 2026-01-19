package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.config.UploadUrlConfig;
import com.chat.realtimechat.exception.FileStorageException;
import com.chat.realtimechat.exception.chat.GroupAccessDeniedException;
import com.chat.realtimechat.exception.chat.GroupNotFoundException;
import com.chat.realtimechat.exception.chat.GroupOperationException;
import com.chat.realtimechat.exception.auth.UserNotFoundException;
import com.chat.realtimechat.model.dto.request.UpdateGroupRequest;
import com.chat.realtimechat.model.dto.response.ChatGroupMemberResponse;
import com.chat.realtimechat.model.dto.response.ChatGroupResponse;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.model.entity.chat.ChatGroup;
import com.chat.realtimechat.model.entity.chat.ChatGroupMember;
import com.chat.realtimechat.model.entity.chat.ChatMessage;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.enums.GroupType;
import com.chat.realtimechat.repository.chat.ChatGroupRepository;
import com.chat.realtimechat.repository.chat.ChatMessageRepository;
import com.chat.realtimechat.repository.users.UserRepository;
import com.chat.realtimechat.util.UploadUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UploadUtils uploadUtils;

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void sendSystemMessage(ChatGroup group, String content) {
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setContent(content);
        systemMsg.setGroup(group);
        systemMsg.setTimestamp(LocalDateTime.now());
        systemMsg.setType(ChatMessage.MessageType.SYSTEM);
        systemMsg.setSender(null);

        ChatMessage saved = chatMessageRepository.save(systemMsg);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(saved);

        group.getMembers().forEach(m -> {
            messagingTemplate.convertAndSendToUser(
                    m.getUser().getUsername(),
                    "/queue/messages",
                    response);
        });
    }

    private ChatGroup getGroupAndValidateAccess(Long groupId, String username) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (group.getType() == GroupType.PRIVATE) {
            throw new GroupOperationException("This is a private chat");
        }

        User actor = findUserByUsername(username);

        if (!group.getUsers().contains(actor)) {
            throw new GroupAccessDeniedException("You are not a member of this group");
        }

        if (!group.getAdmins().contains(actor)) {
            throw new GroupAccessDeniedException("Only admin can modify the group");
        }

        return group;
    }

    private void notifyMembers(ChatGroup group, Set<User> recipients, String senderUsername) {
        ChatGroupResponse response = ChatGroupResponse.fromEntity(group);

        for (User member : recipients) {
            if (!member.getUsername().equals(senderUsername)) {
                messagingTemplate.convertAndSendToUser(
                        member.getUsername(),
                        "/queue/groups",
                        response);
            }
        }
    }

    @Override
    @Transactional
    public ChatGroup createGroup(String creatorUsername, String groupName, Set<Long> memberIds, MultipartFile image) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = uploadUtils.saveFile(image);
            } catch (IOException e) {
                throw new FileStorageException("Failed to upload image", e);
            }
        }

        User creator = findUserByUsername(creatorUsername);

        ChatGroup group = new ChatGroup();
        group.setName(groupName);
        group.setType(GroupType.GROUP);
        group.setImageUrl(imageUrl);
        group.setCreatedAt(LocalDateTime.now());
        group.setColorTheme("#3B82F6");

        group.setAdmins(new HashSet<>(Set.of(creator)));
        group.addMember(creator);
        List<User> others = userRepository.findAllById(memberIds);
        others.forEach(group::addMember);

        ChatGroup savedGroup = chatGroupRepository.save(group);

        notifyMembers(savedGroup, new HashSet<>(others), creatorUsername);

        return savedGroup;
    }

    @Override
    public ChatGroup createPrivateGroup(User user1, User user2) {
        Optional<ChatGroup> existing = chatGroupRepository.findPrivateGroup(user1, user2);

        if (existing.isPresent()) {
            return existing.get();
        }

        ChatGroup group = new ChatGroup();
        group.setName("Private: " + user1.getUsername() + '-' + user2.getUsername());
        group.setType(GroupType.PRIVATE);
        group.setCreatedAt(LocalDateTime.now());
        group.setColorTheme("#3B82F6");
        group.addMember(user1);
        group.addMember(user2);

        return chatGroupRepository.save(group);
    }

    @Override
    public Page<ChatGroupResponse> getUserGroupsResponse(String username, Pageable pageable) {
        User user = findUserByUsername(username);

        Page<ChatGroup> groups = chatGroupRepository.findUserGroups(user, pageable);

        return groups.map(group -> {
            ChatGroupResponse dto = ChatGroupResponse.fromEntity(group);

            dto.setMembers(
                    group.getMembers().stream().map(ChatGroupMemberResponse::fromEntity).collect(Collectors.toSet()));

            dto.setLastMessage(group.getLastMessageContent());
            dto.setLastMessageTime(group.getLastMessageTime());

            if (group.getType().equals(GroupType.PRIVATE)) {
                User otherUser = group.getMembers().stream()
                        .map(ChatGroupMember::getUser)
                        .filter(u -> !u.getId().equals(user.getId()))
                        .findFirst()
                        .orElse(user);

                dto.setName(otherUser.getName() + " " + otherUser.getSurname());
            }

            ChatGroupMember membership = group.getMembers().stream()
                    .filter(m -> m.getUser().getId().equals(user.getId()))
                    .findFirst()
                    .orElseThrow();

            long count = chatMessageRepository.countUnreadMessages(
                    group.getId(),
                    user.getId(),
                    membership.getLastReadTime());
            dto.setUnreadCount((int) count);
            dto.setMuted(membership.isMuted());
            return dto;
        });
    }

    @Override
    public ChatGroupResponse getGroupInfo(Long groupId, String username) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        User user = findUserByUsername(username);

        if (!group.getUsers().contains(user)) {
            throw new GroupAccessDeniedException("You are not a member of this group");
        }

        ChatGroupResponse response = ChatGroupResponse.fromEntity(group);

        ChatGroupMember membership = group.getMembers().stream()
                .filter(m -> m.getUser().getUsername().equals(username))
                .findFirst()
                .orElse(null);
        if (membership != null) {
            response.setMuted(membership.isMuted());
        }

        if (group.getType() == GroupType.PRIVATE) {
            User otherUser = group.getMembers().stream()
                    .map(ChatGroupMember::getUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(user);
            response.setName(otherUser.getName() + " " + otherUser.getSurname());
        }

        chatMessageRepository.findFirstByGroupIdOrderByTimestampDesc(group.getId())
                .ifPresent(message -> {
                    response.setLastMessage(message.getContent());
                    response.setLastMessageTime(message.getTimestamp());
                });

        return response;
    }

    @Override
    @Transactional
    public ChatGroup updateGroup(Long groupId, UpdateGroupRequest request, String username, MultipartFile image) {
        ChatGroup group = getGroupAndValidateAccess(groupId, username);
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = uploadUtils.saveFile(image);
                group.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new FileStorageException("Failed to upload image");
            }
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            String oldName = group.getName();
            group.setName(request.getName());

            sendSystemMessage(group, "User " + username + " changed group name to: " + request.getName());
        }

        ChatGroup savedGroup = chatGroupRepository.save(group);

        String fullImageUrl = null;
        if (savedGroup.getImageUrl() != null && !savedGroup.getImageUrl().isEmpty()) {
            fullImageUrl = UploadUrlConfig.getUploadUrl() + savedGroup.getImageUrl();
        }

        Map<String, Object> event = new HashMap<>();
        event.put("type", "GROUP_UPDATED");
        event.put("groupId", groupId);
        event.put("name", savedGroup.getName());
        event.put("imageUrl", fullImageUrl);

        savedGroup.getMembers().forEach(m -> {
            messagingTemplate.convertAndSendToUser(
                    m.getUser().getUsername(),
                    "/queue/messages",
                    event);
        });

        return savedGroup;
    }

    @Override
    @Transactional
    public void addMembers(Long groupId, Set<Long> memberIds, String username) {
        ChatGroup group = getGroupAndValidateAccess(groupId, username);
        List<User> newMembers = userRepository.findAllById(memberIds);
        newMembers.forEach(group::addMember);
        chatGroupRepository.save(group);

        String addedNames = newMembers.stream()
                .map(User::getUsername)
                .reduce((a, b) -> a + ", " + b)
                .orElse("someone");

        sendSystemMessage(group, username + " added: " + addedNames);

        List<ChatGroupMemberResponse> addedMembersDto = group.getMembers().stream()
                .filter(m -> memberIds.contains(m.getUser().getId()))
                .map(ChatGroupMemberResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> event = new HashMap<>();
        event.put("type", "MEMBER_ADDED");
        event.put("groupId", groupId);
        event.put("members", addedMembersDto);

        group.getMembers().forEach(m -> {
            messagingTemplate.convertAndSendToUser(
                    m.getUser().getUsername(),
                    "/queue/messages",
                    event);
        });

        notifyMembers(group, new HashSet<>(newMembers), username);

    }

    @Override
    @Transactional
    public void removeMembers(Long groupId, Set<Long> memberIds, String username) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        User actor = findUserByUsername(username);

        if (group.getType().equals(GroupType.PRIVATE)) {
            throw new GroupOperationException("This is a private chat");
        }

        boolean removingSelf = memberIds.size() == 1 && memberIds.contains(actor.getId());

        if (!removingSelf && !group.getAdmins().contains(actor)) {
            throw new GroupAccessDeniedException("Only admins can remove other members");
        }

        List<User> usersToRemove = userRepository.findAllById(memberIds);

        usersToRemove.forEach(u -> {
            group.removeMember(u);
            group.getAdmins().remove(u);
        });

        if (group.getMembers().isEmpty()) {
            group.setDeleted(true);
            chatGroupRepository.save(group);
            return;
        }

        if (group.getAdmins().isEmpty()) {
            User newAdmin = group.getMembers().iterator().next().getUser();

            group.getAdmins().add(newAdmin);
        }

        Map<String, Object> event = new HashMap<>();
        event.put("type", "MEMBER_REMOVED");
        event.put("groupId", groupId);
        event.put("userIds", memberIds);

        group.getMembers().forEach(m -> {
            messagingTemplate.convertAndSendToUser(
                    m.getUser().getUsername(),
                    "/queue/messages",
                    event);
        });

        for (User u : usersToRemove) {
            messagingTemplate.convertAndSendToUser(
                    u.getUsername(),
                    "/queue/groups",
                    Map.of(
                            "groupId", groupId,
                            "eventType", "REMOVE"));
        }

        chatGroupRepository.save(group);

        if (removingSelf) {
            sendSystemMessage(group, username + " left the group.");
        } else {
            String removedNames = usersToRemove.stream()
                    .map(User::getUsername)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("someone");
            sendSystemMessage(group, username + " removed: " + removedNames);
        }

    }

    @Override
    public void addAdmin(Long groupId, Long userId, String requestorUsername) {
        ChatGroup group = getGroupAndValidateAccess(groupId, requestorUsername);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found"));

        group.getAdmins().add(user);
        chatGroupRepository.save(group);
    }

    @Override
    public void removeAdmin(Long groupId, Long userId, String requestorUsername) {
        ChatGroup group = getGroupAndValidateAccess(groupId, requestorUsername);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found"));

        if (user.getUsername().equals(requestorUsername) && group.getAdmins().size() == 1) {
            throw new GroupOperationException(
                    "Please nominate a new admin before removing an admin role from yourself");
        }

        group.getAdmins().remove(user);
        chatGroupRepository.save(group);
    }

    @Override
    @Transactional
    public void markAsRead(Long groupId, String username) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        ChatGroupMember member = group.getMembers().stream()
                .filter(m -> m.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new GroupAccessDeniedException("User not in group"));

        if (group.getLastMessageTime() != null && member.getLastReadTime() != null) {
            if (member.getLastReadTime().isAfter(group.getLastMessageTime())) {
                return;
            }
        }

        member.setLastReadTime(LocalDateTime.now());

        ChatGroupMemberResponse memberDto = ChatGroupMemberResponse.fromEntity(member);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "READ_RECEIPT");
        payload.put("groupId", groupId);
        payload.put("member", memberDto);

        group.getMembers().forEach(m -> {
            messagingTemplate.convertAndSendToUser(
                    m.getUser().getUsername(),
                    "/queue/messages",
                    payload);
        });

    }

    @Override
    public void muteGroup(Long groupId, String username) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        ChatGroupMember member = group.getMembers().stream()
                .filter(m -> m.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new GroupAccessDeniedException("User not in group"));

        member.setMuted(true);
        chatGroupRepository.save(group);
    }

    @Override
    public void unmuteGroup(Long groupId, String username) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        ChatGroupMember member = group.getMembers().stream()
                .filter(m -> m.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new GroupAccessDeniedException("User not in group"));

        member.setMuted(false);
        chatGroupRepository.save(group);
    }

    @Override
    public void setColorTheme(Long groupId, String newColorTheme) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        group.setColorTheme(newColorTheme);
        chatGroupRepository.save(group);
    }

    @Override
    public String getColorTheme(Long groupId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        String colorTheme = group.getColorTheme();

        return colorTheme;
    }
}
