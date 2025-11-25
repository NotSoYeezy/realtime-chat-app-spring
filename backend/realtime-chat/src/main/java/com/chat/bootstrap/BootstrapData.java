package com.chat.bootstrap;

import com.chat.domain.User;
import com.chat.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class BootstrapData implements CommandLineRunner {
    private final UserRepository userRepository;

    public BootstrapData(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        User user = new User();
        user.setSurname("Surname");
        user.setEmail("Email");
        user.setPassword("Password");
        userRepository.save(user);
    }
}


