package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.CreateGroupRequest;
import com.chat.realtimechat.model.dto.response.ChatGroupResponse;
import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.repository.ChatGroupRepository;
import com.chat.realtimechat.repository.ChatMessageRepository;
import com.chat.realtimechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ChatGroupResponse>> getGroups(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: REFACTOR THAT TO SERVICE
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ChatGroup> groups = chatGroupRepository.findAllByMembersContaining(user);

        List<ChatGroupResponse> response = groups.stream()
                .map(group -> {
                    ChatGroupResponse dto = ChatGroupResponse.fromEntity(group);

                    chatMessageRepository.findFirstByGroupIdOrderByTimestampDesc(group.getId())
                            .ifPresent(message -> {
                                dto.setLastMessage(message.getContent());
                                dto.setLastMessageTime(message.getTimestamp());
                            });

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ChatGroupResponse> createGroup(@RequestBody CreateGroupRequest request,
                                                         @AuthenticationPrincipal UserDetails userDetails) {

        User creator = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatGroup group = new ChatGroup();
        group.setName(request.getName());

        List<User> members = userRepository.findAllById(request.getMemberIds());
        Set<User> membersSet =  new HashSet<>(members);
        membersSet.add(creator);

        group.setMembers(new HashSet<>(membersSet));

        ChatGroup savedGroup = chatGroupRepository.save(group);
        ChatGroupResponse response = ChatGroupResponse.fromEntity(savedGroup);

        // TODO: NOTIFICATION WHEN ADDED
//        NotificationResponse notification = new NotificationResponse(
//                NotificationResponse.NotificationType.ADDED_TO_GROUP,
//                response
//        );

//        for (User member : savedGroup.getMembers()) {
//            messagingTemplate.convertAndSendToUser(
//                    member.getUsername(),
//                    "/queue/messages",
//                    notification
//            );
//        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
