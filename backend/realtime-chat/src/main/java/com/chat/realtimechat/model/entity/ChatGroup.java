package com.chat.realtimechat.model.entity;

import com.chat.realtimechat.model.enums.GroupType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "chat_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private GroupType type;

    private String imageUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_admins",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatGroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "last_message_content")
    private String lastMessageContent;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ChatGroup chatGroup)) return false;

        return getId().equals(chatGroup.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public void addMember(User user) {
        ChatGroupMember member = new ChatGroupMember(this, user);
        members.add(member);
    }

    public void removeMember(User user) {
        ChatGroupMember memberToRemove = this.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
        if (memberToRemove != null) {
            this.getMembers().remove(memberToRemove);
        }
    }

    public Set<User> getUsers() {
        return members.stream().map(ChatGroupMember::getUser).collect(Collectors.toSet());
    }
}
