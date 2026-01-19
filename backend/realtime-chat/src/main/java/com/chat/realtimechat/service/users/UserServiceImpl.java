package com.chat.realtimechat.service.users;

import com.chat.realtimechat.exception.auth.EmailAlreadyExistsException;
import com.chat.realtimechat.exception.auth.IncorrectPasswordException;
import com.chat.realtimechat.exception.auth.LoginUserNotFoundException;
import com.chat.realtimechat.exception.auth.UserNotConfirmedException;
import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.dto.response.UserResponse;
import com.chat.realtimechat.model.entity.ChatGroup;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.entity.auth.PasswordResetToken;
import com.chat.realtimechat.repository.security.PasswordResetTokenRepository;
import com.chat.realtimechat.model.dto.request.UpdateUserRequest;
import com.chat.realtimechat.repository.users.UserRepository;
import com.chat.realtimechat.service.security.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.text.Normalizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FriendshipService friendshipService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final GoogleRefreshTokenRepository googleRefreshTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatGroupMemberRepository chatGroupMemberRepository;

    @Override
    public Iterable<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findUsersByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User registerUser(RegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            User user = userRepository.findByEmail(request.getEmail()).get();
            if (user.getConfirmed()) {
                throw new EmailAlreadyExistsException(request.getEmail());
            } else {
                throw new UserNotConfirmedException("User is not confirmed!");
            }
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setUsername(generateUsername(request.getName(), request.getSurname()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public User registeredGoogleUser(OAuth2User oAuth2User) {
        String id = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");

        if (userRepository.findByProviderId(id).isPresent()) {
            return userRepository.findByProviderId(id).get();
        } else if (userRepository.findByEmail(email).isPresent()) {
            User updatedUser = userRepository.findByEmail(email).get();
            updatedUser.setProviderId(id);
            return userRepository.save(updatedUser);
        }
        String name = oAuth2User.getAttribute("name");
        String firstName = name.split(" ")[0];
        String lastName = firstName;
        if (name.split(" ").length > 1) {
            lastName = name.split(" ")[1];
        }

        User user = new User();
        user.setEmail(email);
        user.setName(firstName);
        user.setSurname(lastName);
        user.setUsername(generateUsername(firstName, lastName));
        user.setProviderId(id);
        user.setConfirmed(true);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteAccount(String username){
        User user = userRepository.findByUsername(username).orElseThrow(LoginUserNotFoundException::new);

        friendshipRepository.deleteByUser(user);
        friendshipRepository.deleteByFriend(user);

        chatGroupMemberRepository.deleteByUser(user);

        List<ChatGroup> adminGroups = chatGroupRepository.findByAdminsContains(user);
        for (ChatGroup group : adminGroups) {
            group.getAdmins().remove(user);
            chatGroupRepository.save(group);
        }

        chatMessageRepository.deleteAllBySender(user);

        if(!user.getProviderId().isEmpty()){
            googleRefreshTokenRepository.deleteByUser(user);
        }

        refreshTokenRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }

    @Override
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(LoginUserNotFoundException::new);
        if (!user.getConfirmed()) {
            throw new UserNotConfirmedException("User is not confirmed!");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        return user;
    }

    @Override
    public Page<UserResponse> getAllUsersResponses(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    @Override
    public Page<FriendUserResponse> searchUsers(String query, UserDetails userDetails, Pageable pageable) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(LoginUserNotFoundException::new);

        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return Page.empty();
        }

        Set<Long> excludedIds = friendshipService.getExcludedUserIds(currentUser.getId());

        return userRepository
                .searchCandidates(q, excludedIds, pageable)
                .map(FriendUserResponse::fromEntity);
    }

    @Override
    public void initiatePasswordReset(String email) {
        passwordResetTokenService.initiatePasswordReset(email);

    }

    @Override
    public void resetPassword(String token, String password) {

        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> // TODO: throw custom error
                new IllegalArgumentException("Invalid password reset token"));

        if (passwordResetToken.isTokenExpired()) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new IllegalArgumentException("Password reset token has expired"); // TODO: throw custom error
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    @Override
    public User updateUser(UpdateUserRequest request, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(LoginUserNotFoundException::new);

        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getSurname() != null) {
            user.setSurname(request.getSurname());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public boolean checkPassword(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new LoginUserNotFoundException();
        }
        String dbPassword = user.get().getPassword();
        if (password == null) {
            return dbPassword != null;
        }
        if (dbPassword == null) {
            return false;
        }
        if (!passwordEncoder.matches(password, dbPassword)) {
            throw new IncorrectPasswordException();
        }
        return true;
    }


    private String generateUsername(String name, String surname) {
        String base = (stripAccents(name) + "-" + stripAccents(surname))
                .toLowerCase()
                .replace(" ", "-");

        String hash = Integer.toHexString((name + surname + System.nanoTime()).hashCode());
        String username = base + "#" + hash.substring(0, 4);

        while (userRepository.existsByUsername(username)) {
            hash = Integer.toHexString((name + surname + System.nanoTime()).hashCode());
            username = base + "#" + hash.substring(0, 4);
        }

        return username;
    }

    private String stripAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{ASCII}]", "");
    }
}
