package com.chat.realtimechat.service;

import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.exception.EmailAlreadyExistsException;
import com.chat.realtimechat.exception.IncorrectPasswordException;
import com.chat.realtimechat.exception.LoginUserNotFoundException;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
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
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setUsername(generateUsername(request.getName(), request.getSurname()));
        user.setSurname(request.getSurname());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(LoginUserNotFoundException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        return user;
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
    public boolean checkPassword(String username, String password){
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new LoginUserNotFoundException();
        }
        if(!passwordEncoder.matches(password,user.get().getPassword())){
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
