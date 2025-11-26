package com.chat.realtimechat.service;

import com.chat.realtimechat.domain.MyUsr;

public interface UserService {
    Iterable<MyUsr> findAllUsers();
}
