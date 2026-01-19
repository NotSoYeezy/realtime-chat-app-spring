package com.chat.realtimechat.model.entity.auth;

import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.entity.common.BaseToken;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "google_refresh_token")
public class GoogleRefreshToken extends BaseToken {
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    private String token;
}