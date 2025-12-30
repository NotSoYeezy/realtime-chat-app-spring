package com.chat.realtimechat.service;

import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.dto.response.UserResponse;
import com.chat.realtimechat.exception.UserNotConfirmedException;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.exception.EmailAlreadyExistsException;
import com.chat.realtimechat.exception.IncorrectPasswordException;
import com.chat.realtimechat.exception.LoginUserNotFoundException;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FriendshipService friendshipService;

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
        String lastName = name.split(" ")[1];

        User user = new User();
        user.setEmail(email);
        user.setName(firstName);
        user.setSurname(lastName);
        user.setUsername(generateUsername(firstName, lastName));
        user.setProviderId(id);
        user.setConfirmed(true);
        return userRepository.save(user);
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
    public List<UserResponse> getAllUsersResponses() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    public List<FriendUserResponse> searchUsers(String query, UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(LoginUserNotFoundException::new);

        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return List.of();
        }

        Set<Long> excludedIds = friendshipService.getExcludedUserIds(currentUser.getId());

        return userRepository
                .searchCandidates(q)
                .stream()
                .filter(u -> !excludedIds.contains(u.getId()))
                .map(FriendUserResponse::fromEntity)
                .toList();
    }


    @Override
    public User updateUser(UpdateRequest request, String username) {
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
        if (password == null && user.get().getPassword() == null) {
            return false;
        }
        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            throw new IncorrectPasswordException();
        }
        return true;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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
