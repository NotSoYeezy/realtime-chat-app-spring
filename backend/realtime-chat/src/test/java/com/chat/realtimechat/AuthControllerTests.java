package com.chat.realtimechat;

import com.chat.realtimechat.controller.security.AuthController;
import com.chat.realtimechat.model.dto.request.LoginRequest;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.security.CustomUserDetailsService;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthControllerTests {

    @Autowired
    private AuthController authController;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "test1234";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    void registerTestUser() {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail(TEST_EMAIL);
        registrationRequest.setPassword(TEST_PASSWORD);
        registrationRequest.setUsername(TEST_USERNAME);
        var response = authController.register(registrationRequest);
//        authController.logoutUser(Map.of("refreshToken", response.getBody().getRefreshToken()));
    }


    @Test
    void testAuthControllerRegister() {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername(TEST_USERNAME);
        registrationRequest.setPassword(TEST_PASSWORD);
        registrationRequest.setEmail(TEST_EMAIL);
        var registeredUser = authController.register(registrationRequest);

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(registeredUser).isNotNull();

        assertThat(registeredUser.getBody().getUsername()).isEqualTo(registrationRequest.getUsername());
        assertThat(registeredUser.getBody().getEmail()).isEqualTo(registrationRequest.getEmail());

        assertThat(userRepository.findByUsername(registrationRequest.getUsername()).get().getEmail()).isEqualTo(registrationRequest.getEmail());
        assertThat(userRepository.findByEmail(registrationRequest.getEmail()).get().getUsername()).isEqualTo(registrationRequest.getUsername());
    }

    @Test
    void testAuthControllerLogin() {
        registerTestUser();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);
        var registeredUser = authController.login(loginRequest);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getBody().getToken()).isNotNull();
//        assertThat(registeredUser.getBody().getRefreshToken()).isNotNull();
    }

    @Test
    void testAuthControllerUpdateUser() {
        registerTestUser();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);
        authController.login(loginRequest);

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setEmail("new_email");
        updateRequest.setUsername("new_username");

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        var updatedUser = authController.update(updateRequest, userDetails);

        assertThat(updatedUser).isNotNull();
    }

}
