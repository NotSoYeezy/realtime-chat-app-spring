package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.exception.GroupNotFoundException;
import com.chat.realtimechat.model.dto.request.ChatMessageRequest;
import com.chat.realtimechat.model.dto.request.StatusUpdateRequest;
import com.chat.realtimechat.model.dto.response.ChatMessageResponse;
import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.ChatMessage;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.ChatGroupRepository;
import com.chat.realtimechat.repository.ChatMessageRepository;
import com.chat.realtimechat.repository.FriendshipRepository;
import com.chat.realtimechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{

    private final ChatMessageRepository chatMessageRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final UserPresenceService presenceService;

    @Override
    @Transactional
    public void sendMessage(Long groupId, ChatMessageRequest request, Principal principal) {
        User sender = getCurrentUser(principal);

        ChatGroup group = chatGroupRepository.findByIdWithMembers(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(request.getContent());
        chatMessage.setSender(sender);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(request.getType());
        chatMessage.setGroup(group);

        if (request.getParentId() != null) {
            chatMessageRepository.findById(request.getParentId())
                    .ifPresent(chatMessage::setParent);
        }

        ChatMessage savedMsg = chatMessageRepository.save(chatMessage);

        group.setLastMessageContent(savedMsg.getContent());
        group.setLastMessageTime(savedMsg.getTimestamp());
        chatGroupRepository.save(group);

        group.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(sender.getId()))
                .findFirst()
                .ifPresent(m -> m.setLastReadTime(LocalDateTime.now()));

        ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMsg);

        messagingTemplate.convertAndSend(
                "/topic/group." + groupId,
                response
        );
    }

    @Override
    public void handleTyping(Long groupId, Principal principal) {
        User user = getCurrentUser(principal);

        ChatMessageResponse notification = new ChatMessageResponse();
        notification.setSender(senderInfo(user));
        notification.setType(ChatMessage.MessageType.TYPING);
        notification.setTimestamp(LocalDateTime.now());
        notification.setGroupId(groupId);

        messagingTemplate.convertAndSend(
                "/topic/group." + groupId,
                notification
        );
    }

    @Override
    public ResponseEntity<List<ChatMessageResponse>> loadGroupHistory(Long groupId, Principal principal) {
        User user = getCurrentUser(principal);
        ChatGroup group = chatGroupRepository.getById(groupId);

        if (!group.getUsers().contains(user)) {
            throw new GroupNotFoundException("Group not found!");
        }

        List<ChatMessage> messages = chatMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);

        return ResponseEntity.status(HttpStatus.OK).body(
                messages.stream()
                        .map(ChatMessageResponse::fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void handleStatusChange(StatusUpdateRequest request, Principal principal) {
        User user = getCurrentUser(principal);
        presenceService.updateUserStatus(user.getUsername(), request.getStatus());

        ChatMessageResponse notification = new ChatMessageResponse();
        notification.setType(ChatMessage.MessageType.JOIN);
        notification.setSender(senderInfo(user));
        notification.setUserStatus(request.getStatus());
        notification.setTimestamp(LocalDateTime.now());

        List<User> friends = friendshipRepository.findFriendsByUserId(user.getId());
        for (User friend : friends) {
             messagingTemplate.convertAndSendToUser(
                     friend.getUsername(),
                     "/queue/friends-status",
                     notification
             );
        }
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(principal.getName()));
    }

    private OnlineInfoResponse senderInfo(User user) {
        return presenceService.getOnlineUsers()
                .getOrDefault(
                        user.getUsername(),
                        new OnlineInfoResponse(
                                user.getName(),
                                user.getSurname(),
                                user.getUsername(),
                                UserStatus.ONLINE
                        )
                );
    }
}