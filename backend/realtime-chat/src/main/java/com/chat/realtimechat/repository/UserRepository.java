package com.chat.realtimechat.repository;

import com.chat.realtimechat.domain.MyUsr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<MyUsr, Long> {
    Optional<MyUsr> findByUsername(String username);
}
