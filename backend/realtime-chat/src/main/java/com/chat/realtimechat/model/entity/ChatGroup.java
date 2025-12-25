package com.chat.realtimechat.model.entity;

import com.chat.realtimechat.model.enums.GroupType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ChatGroup chatGroup)) return false;

        return getId().equals(chatGroup.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
