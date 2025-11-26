package com.chat.realtimechat.service;

import com.chat.realtimechat.domain.User;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.repository.UserRepository;
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
    public User registerUser(RegistrationRequest request){
        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new RuntimeException("Username is already in use");
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
    public User authenticate(String username, String password){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));
        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Incorrect password");
        }
        return user;
    }

//    @Override
//    public User updateUser(User user){
//
//    }

    @Override
    public void deleteUser(Long id){

    }
}
