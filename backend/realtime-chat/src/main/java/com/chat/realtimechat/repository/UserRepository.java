package com.chat.realtimechat.repository;

import com.chat.realtimechat.domain.MyUsr;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<MyUsr, Long> {
}
