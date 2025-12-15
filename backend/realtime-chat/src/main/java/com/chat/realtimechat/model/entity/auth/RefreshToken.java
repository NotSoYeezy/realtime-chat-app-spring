package com.chat.realtimechat.model.entity.auth;

import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.entity.common.BaseToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends BaseToken {
    @ManyToOne
    @JoinColumn(name = "app_user_id", referencedColumnName = "id", nullable = false)
    private User user;
}