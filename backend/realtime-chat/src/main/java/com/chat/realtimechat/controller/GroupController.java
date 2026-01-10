package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.request.UserIdRequest;
import com.chat.realtimechat.model.dto.request.ChatGroupRequest;
import com.chat.realtimechat.model.dto.request.GroupMembersRequest;
import com.chat.realtimechat.model.dto.request.UpdateGroupRequest;
import com.chat.realtimechat.model.dto.response.ChatGroupResponse;
import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.repository.ChatGroupRepository;
import com.chat.realtimechat.repository.ChatMessageRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.chat.GroupService;
import com.chat.realtimechat.util.UploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ChatGroupResponse>> getGroups(@AuthenticationPrincipal UserDetails userDetails) {
        List<ChatGroupResponse> response = groupService.getUserGroupsResponse(userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ChatGroupResponse> getGroupInfo(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                groupService.getGroupInfo(groupId, userDetails.getUsername())
        );
    }

    @PostMapping
    public ResponseEntity<ChatGroupResponse> createGroup(
            @RequestPart("data") ChatGroupRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        ChatGroup group = groupService.createGroup(
                userDetails.getUsername(),
                request.getName(),
                request.getMemberIds(),
                image
        );

        ChatGroupResponse response = ChatGroupResponse.fromEntity(group);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ChatGroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestPart("data") UpdateGroupRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatGroup updatedGroup = groupService.updateGroup(
                groupId,
                request,
                userDetails.getUsername(),
                image
        );

        return ResponseEntity.status(HttpStatus.OK).body(ChatGroupResponse.fromEntity(updatedGroup));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMembers(
            @PathVariable Long groupId,
            @RequestBody GroupMembersRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupService.addMembers(groupId, request.getUserIds(), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body("Members added");
    }

    @DeleteMapping("/{groupId}/members")
    public ResponseEntity<?> removeMembers(
            @PathVariable Long groupId,
            @RequestBody GroupMembersRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupService.removeMembers(groupId, request.getUserIds(), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body("Members removed");
    }

    @PostMapping("/{groupId}/admin")
    public ResponseEntity<?> addAdmin(
            @PathVariable Long groupId,
            @RequestBody UserIdRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupService.addAdmin(groupId, request.getUserId(),  userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body("User is no longer an admin");
    }

    @DeleteMapping("/{groupId}/admin")
    public ResponseEntity<?> removeAdmin(
            @PathVariable Long groupId,
            @RequestBody UserIdRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupService.removeAdmin(groupId, request.getUserId(),  userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body("User is no longer an admin");
    }

    @PostMapping("/{groupId}/read")
    public ResponseEntity<?> markRead(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        groupService.markAsRead(groupId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/mute")
    public ResponseEntity<?> muteGroup(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        groupService.muteGroup(groupId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/mute")
    public ResponseEntity<?> unmuteGroup(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        groupService.unmuteGroup(groupId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/setColorTheme")
    public ResponseEntity<?> setColorTheme(@PathVariable Long groupId, @RequestBody Map<String, String> payload) {
        groupService.setColorTheme(groupId, payload.get("color"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/getColorTheme")
    public ResponseEntity<?> getColorTheme(@PathVariable Long groupId) {
        String colorTheme = groupService.getColorTheme(groupId);
        return ResponseEntity.ok().body(colorTheme);
    }

}
