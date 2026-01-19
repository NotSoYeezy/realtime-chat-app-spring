package com.chat.realtimechat.service.users;

import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.dto.response.UserResponse;
import com.chat.realtimechat.model.entity.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateUserRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

public interface UserService {
    Iterable<User> findAllUsers();

    Optional<User> findUsersByUsername(String username);

    User registerUser(RegistrationRequest request);

    User updateUser(UpdateUserRequest request, String username);

    User registeredGoogleUser(OAuth2User user);

    void deleteUser(Long id);

    boolean checkPassword(String username, String password);

    User authenticate(String username, String password);

    Page<UserResponse> getAllUsersResponses(Pageable pageable);

    Page<FriendUserResponse> searchUsers(String query, UserDetails userDetails, Pageable pageable)
            throws UsernameNotFoundException;

    void initiatePasswordReset(String email);

    void resetPassword(String passwordResetToken, String password);
}
