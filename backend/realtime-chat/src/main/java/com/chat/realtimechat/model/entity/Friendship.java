package com.chat.realtimechat.model.entity;

import com.chat.realtimechat.model.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    private FriendshipStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();
}
