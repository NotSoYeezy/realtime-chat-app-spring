package com.chat.realtimechat.model.entity;

import com.chat.realtimechat.model.enums.MessageContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name= "chat_group_id")
    private ChatGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChatMessage parent;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    private MessageContentType contentType;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        SYSTEM
    }
}