package com.chat.realtimechat.service;

import com.chat.realtimechat.domain.User;

public interface UserService {
    Iterable<User> findAllUsers();
}
