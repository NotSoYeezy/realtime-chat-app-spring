package com.chat.realtimechat.model.entity.auth;

import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.entity.common.BaseToken;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken extends BaseToken {

    @ManyToOne
    @JoinColumn(name = "app_user_id", nullable = false)
    private User user;
}
