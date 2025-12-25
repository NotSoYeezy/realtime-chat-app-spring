package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    List<User> NameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname);
    Optional<User> findByProviderId(String id);
    List<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String username, String name, String surname);
}
