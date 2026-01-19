package com.chat.realtimechat;


import com.chat.realtimechat.controller.security.AuthController;
import com.chat.realtimechat.repository.security.RefreshTokenRepository;
import com.chat.realtimechat.repository.users.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class ValidationExceptionTests {

    @Autowired
    private MockMvc mockMvc;
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
    void RegistrationRequestValidationExceptionTest() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                              "username": "testuser",
                              "password": "1234456789",
                              "email": "testexample.com"
                            }
                        """)
        ).andExpect(status().isBadRequest());
    }
}
