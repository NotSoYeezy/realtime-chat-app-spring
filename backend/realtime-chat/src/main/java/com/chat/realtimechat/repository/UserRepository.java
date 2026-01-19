package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByProviderId(String id);

    void deleteByUsername(String username);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT(:query, '%'))
               OR LOWER(u.surname)  LIKE LOWER(CONCAT(:query, '%'))
            ORDER BY
                CASE WHEN u.lastSeen IS NULL THEN 1 ELSE 0 END,
                u.lastSeen DESC
            """)
    List<User> searchCandidates(@Param("query") String q);
}
