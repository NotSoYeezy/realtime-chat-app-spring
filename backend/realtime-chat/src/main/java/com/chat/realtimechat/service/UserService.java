package com.chat.realtimechat.service;

import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.dto.response.UserResponse;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Iterable<User> findAllUsers();
    Optional<User> findUsersByUsername(String username);
    User registerUser(RegistrationRequest request);
    User updateUser(UpdateRequest request, String username);
    User registeredGoogleUser(OAuth2User user);
    void deleteUser(Long id);
    boolean checkPassword(String username, String password);
    User authenticate(String username, String password);
    List<UserResponse> getAllUsersResponses();
    List<FriendUserResponse> searchUsers(String query);
}
