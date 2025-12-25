package com.chat.realtimechat.controller;

import com.chat.realtimechat.model.dto.response.FriendUserResponse;
import com.chat.realtimechat.model.dto.response.UserResponse;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // TODO: PAGINATION AND PARAM QUERYING!
        List<User> users = userRepository.findAll();

        List<UserResponse> response = users.stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getUsername(),
                        u.getName(),
                        u.getSurname()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        UserResponse registeredUserResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getSurname()
        );
        return ResponseEntity.status(HttpStatus.OK).body(registeredUserResponse);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(
                userService.getAllUsersResponses()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<FriendUserResponse>> searchUsers(
            @RequestParam String query
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                userService.searchUsers(query)
        );
    }


}
