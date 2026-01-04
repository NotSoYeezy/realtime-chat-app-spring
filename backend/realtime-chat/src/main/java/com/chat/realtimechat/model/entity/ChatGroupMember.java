package com.chat.realtimechat.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
public class ChatGroupMember {

    @EmbeddedId
    private ChatGroupMemberKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private ChatGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime joinedAt = LocalDateTime.now();

    private LocalDateTime lastReadTime = LocalDateTime.now();

    @Column(name = "is_muted")
    private boolean isMuted = false;

    public ChatGroupMember(ChatGroup group, User user) {
        this.group = group;
        this.user = user;
        this.id = new ChatGroupMemberKey(group.getId(), user.getId());
        this.lastReadTime = LocalDateTime.now();
    }
}