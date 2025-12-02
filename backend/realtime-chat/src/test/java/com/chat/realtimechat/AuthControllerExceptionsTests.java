package com.chat.realtimechat;

import com.chat.realtimechat.controller.security.AuthController;
import com.chat.realtimechat.exception.IncorrectPasswordException;
import com.chat.realtimechat.exception.LoginUserNotFoundException;
import com.chat.realtimechat.exception.UserAlreadyExistsException;
import jakarta.validation.ValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import com.chat.realtimechat.model.dto.request.LoginRequest;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateRequest;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthControllerExceptionsTests {

    @Autowired
    private AuthController authController;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "test1234";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeAll
    public void initialize() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername(TEST_USERNAME);
        registrationRequest.setPassword(TEST_PASSWORD);
        registrationRequest.setEmail(TEST_EMAIL);
        authController.register(registrationRequest);
    }

    @Test
    public void authControllerUserAlreadyExistsExceptionTest() {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername(TEST_USERNAME);
        registrationRequest.setPassword(TEST_PASSWORD);
        registrationRequest.setEmail(TEST_EMAIL);

        assertThatThrownBy(() -> authController.register(registrationRequest))
                                .isInstanceOf(UserAlreadyExistsException.class)
                                .hasMessageContaining("user with username " + TEST_USERNAME + " already exists");
    }

    @Test
    public void authControllerLoginUserNotFoundExceptionTest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("11@11");
        loginRequest.setPassword("11");

        assertThatThrownBy(() -> authController.login(loginRequest))
                                .isInstanceOf(LoginUserNotFoundException.class);
    }

    @Test
    public void authcontrollerIncorrectPasswordExceptionTest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("11");

        assertThatThrownBy(() -> authController.login(loginRequest))
                .isInstanceOf(IncorrectPasswordException.class);
    }
}














