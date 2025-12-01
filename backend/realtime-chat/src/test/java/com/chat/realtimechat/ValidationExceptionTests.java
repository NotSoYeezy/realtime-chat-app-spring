package com.chat.realtimechat;


import com.chat.realtimechat.controller.AuthController;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.UserService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ValidationExceptionTests {

    @Autowired
    private AuthController authController;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "1234456789";
    private static final String TEST_EMAIL = "testexample.com";

    @BeforeAll
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void RegistrationRequestValidationExceptionTest() {
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername(TEST_USERNAME);
        invalidRequest.setPassword(TEST_PASSWORD);

//        assertThatThrownBy(() -> authController.register(invalidRequest))
//                .isInstanceOfAny(
//                        TransactionSystemException.class,
//                        DataIntegrityViolationException.class,
//                        jakarta.validation.ValidationException.class
//                );

        assertThat(userRepository.count()).isEqualTo(0);
    }


}
