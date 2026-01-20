package com.chat.realtimechat.repository.users;

import com.chat.realtimechat.model.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
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
            WHERE (LOWER(u.name) LIKE LOWER(CONCAT(:query, '%'))
               OR LOWER(u.surname)  LIKE LOWER(CONCAT(:query, '%')))
               AND u.id NOT IN :excludedIds
            """)
    Page<User> searchCandidates(@Param("query") String q, @Param("excludedIds") Collection<Long> excludedIds, Pageable pageable);
}
