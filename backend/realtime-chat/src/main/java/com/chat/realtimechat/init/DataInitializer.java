package com.chat.realtimechat.init;

import com.chat.realtimechat.model.entity.users.Friendship;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.enums.FriendshipStatus;
import com.chat.realtimechat.repository.users.FriendshipRepository;
import com.chat.realtimechat.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByUsername("demo")) {
            System.out.println(">>> DEMO DATA ALREADY EXISTS – SKIPPING SEED <<<");
            return;
        }

        System.out.println(">>> DEMO SEED STARTED <<<");

        String pwd = passwordEncoder.encode("demo");

        User demo = user("demo", "Demo", "User", "demo@gmail.com", pwd);

        User anna = user("anna", "Anna", "Smith", "anna@gmail.com", pwd);
        User brian = user("brian", "Brian", "Johnson", "brian@gmail.com", pwd);
        User celine = user("celine", "Celine", "Brown", "celine@gmail.com", pwd);
        User daniel = user("daniel", "Daniel", "Wilson", "daniel@gmail.com", pwd);
        User eva = user("eva", "Eva", "Taylor", "eva@gmail.com", pwd);

        User philip = user("philip", "Philip", "Anderson", "philip@gmail.com", pwd);
        User gabriel = user("gabriel", "Gabriel", "Martinez", "gabriel@gmail.com", pwd);
        User hannah = user("hannah", "Hannah", "Moore", "hannah@gmail.com", pwd);
        User igor = user("igor", "Igor", "Miller", "igor@gmail.com", pwd);
        User julia = user("julia", "Julia", "Davis", "julia@gmail.com", pwd);
        User jacob = user("jacob", "Jacob", "Clark", "jacob@gmail.com", pwd);
        User lena = user("lena", "Lena", "Walker", "lena@gmail.com", pwd);
        User matthew = user("matthew", "Matthew", "Hall", "matthew@gmail.com", pwd);
        User natalie = user("natalie", "Natalie", "Lewis", "natalie@gmail.com", pwd);
        User olaf = user("olaf", "Olaf", "Young", "olaf@gmail.com", pwd);

        save(demo);

        save(anna);
        save(brian);
        save(celine);
        save(daniel);
        save(eva);

        save(philip);
        save(gabriel);
        save(hannah);
        save(igor);
        save(julia);
        save(jacob);
        save(lena);
        save(matthew);
        save(natalie);
        save(olaf);

        accepted(demo, anna);
        accepted(demo, brian);
        accepted(demo, philip);
        accepted(demo, julia);
        accepted(demo, jacob);
        accepted(demo, hannah);

        pending(celine, demo);
        pending(demo, daniel);
        pending(igor, demo);
        pending(demo, lena);
        pending(natalie, demo);

        blocked(demo, eva);
        blocked(demo, olaf);
        blocked(demo, matthew);

        System.out.println(">>> DEMO USER READY <<<");
    }

    private void save(User u) {
        userRepository.save(u);
    }

    private User user(String username, String name, String surname, String email, String password) {
        User u = new User();
        u.setUsername(username);
        u.setName(name);
        u.setSurname(surname);
        u.setEmail(email);
        u.setPassword(password);
        u.setConfirmed(true);
        return u;
    }

    private void accepted(User a, User b) {
        friendshipRepository.save(friendship(a, b, FriendshipStatus.ACCEPTED));
    }

    private void pending(User from, User to) {
        friendshipRepository.save(friendship(from, to, FriendshipStatus.PENDING));
    }

    private void blocked(User blocker, User blocked) {
        friendshipRepository.save(friendship(blocker, blocked, FriendshipStatus.BLOCKED));
    }

    private Friendship friendship(User u, User f, FriendshipStatus status) {
        Friendship fr = new Friendship();
        fr.setUser(u);
        fr.setFriend(f);
        fr.setStatus(status);
        return fr;
    }
}