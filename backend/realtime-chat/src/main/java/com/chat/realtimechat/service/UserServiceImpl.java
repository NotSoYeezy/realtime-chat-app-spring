package com.chat.realtimechat.service;

import com.chat.realtimechat.domain.MyUsr;
import com.chat.realtimechat.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Iterable<MyUsr> findAllUsers() {
        return userRepository.findAll();
    }
}
