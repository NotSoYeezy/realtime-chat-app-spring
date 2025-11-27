package com.chat.realtimechat.service;

import com.chat.realtimechat.domain.User;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;

import java.util.Optional;

public interface UserService {
    Iterable<User> findAllUsers();
    Optional<User> findUsersByUsername(String username);
    User registerUser(RegistrationRequest request);
    User updateUser(User user);
    void deleteUser(Long id);
    User authenticate(String username, String password);
}
