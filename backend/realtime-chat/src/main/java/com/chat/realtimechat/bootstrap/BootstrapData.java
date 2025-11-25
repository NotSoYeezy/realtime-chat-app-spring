package com.chat.realtimechat.bootstrap;

import com.chat.realtimechat.domain.MyUsr;
import com.chat.realtimechat.repository.UserRepository;
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
        MyUsr myUsr = new MyUsr();
        myUsr.setSurname("Surname");
        myUsr.setEmail("Email");
        myUsr.setPassword("Password");
        userRepository.save(myUsr);
    }
}


