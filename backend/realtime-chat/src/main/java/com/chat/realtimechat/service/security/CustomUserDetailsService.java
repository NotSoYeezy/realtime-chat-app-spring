package com.chat.realtimechat.service.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {
    UserDetails loadUserByUsername(String username);
    UserDetails loadUserById(Long id);
}
