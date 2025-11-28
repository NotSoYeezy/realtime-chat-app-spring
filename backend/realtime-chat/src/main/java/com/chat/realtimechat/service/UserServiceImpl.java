package com.chat.realtimechat.service;

import com.chat.realtimechat.domain.User;
import com.chat.realtimechat.exception.IncorrectPasswordException;
import com.chat.realtimechat.exception.LoginUserNotFoundException;
import com.chat.realtimechat.exception.UserAlreadyExistsException;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setSurname(request.getSurname());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(LoginUserNotFoundException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        return user;
    }

    @Override
    public User updateUser(UpdateRequest request, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(LoginUserNotFoundException::new);

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getSurname() != null) {
            user.setSurname(request.getSurname());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getUsername() != null) {
            String newUsername = request.getUsername();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    throw new UserAlreadyExistsException(newUsername);
                }
                user.setUsername(newUsername);
            }
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
