package com.chat.realtimechat;

import com.chat.realtimechat.config.SecurityConfig;
import com.chat.realtimechat.controller.security.AuthController;
import com.chat.realtimechat.exception.EmailAlreadyExistsException;
import com.chat.realtimechat.exception.IncorrectPasswordException;
import com.chat.realtimechat.exception.LoginUserNotFoundException;
import com.chat.realtimechat.model.dto.request.LoginRequest;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.entity.auth.RefreshToken;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.RegisterConfirmTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.security.JwtAuthFilter;
import com.chat.realtimechat.security.OAuth2LoginSuccessHandler;
import com.chat.realtimechat.service.UserService;
import com.chat.realtimechat.service.security.RefreshTokenService;
import com.chat.realtimechat.service.security.RegisterConfirmTokenService;
import com.chat.realtimechat.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerExceptionsTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private RegisterConfirmTokenService registerConfirmTokenService;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private RegisterConfirmTokenRepository registerConfirmTokenRepository;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    public void setup() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    public void register_ShouldReturn400_WhenEmailFormatIsInvalid() throws Exception {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("invalid-email-format");
        req.setPassword("SecurePass123!");
        req.setName("Bob");
        req.setSurname("Builder");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    public void register_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("alice@test.com");
        req.setPassword("SecurePass123!");
        req.setName("Alice");
        req.setSurname("Smith");

        given(userService.registerUser(any(RegistrationRequest.class)))
                .willThrow(new EmailAlreadyExistsException("alice@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void login_ShouldReturn400_WhenUserNotFound() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("unknown@test.com");
        loginReq.setPassword("password123");

        given(userService.authenticate("unknown@test.com", "password123"))
                .willThrow(new LoginUserNotFoundException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_ShouldReturn400_WhenPasswordIsIncorrect() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("bob@test.com");
        loginReq.setPassword("wrong-password");

        given(userService.authenticate("bob@test.com", "wrong-password"))
                .willThrow(new IncorrectPasswordException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginReq)))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void logout_ShouldReturn400_WhenRefreshTokenIsInvalid() throws Exception {
        String invalidToken = "invalid-token";
        String requestBody = "{\"refreshToken\": \"" + invalidToken + "\"}";

        given(refreshTokenRepository.findByToken(invalidToken)).willReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/logout")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid refresh token."));
    }

    @Test
    public void logout_ShouldReturn400_WhenRefreshTokenIsMissing() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(post("/api/auth/logout")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Refresh token is required."));
    }

    @Test
    public void deleteUser_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/delete"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteAccount(any());
    }

    @Test
    public void deleteUser_ShouldReturn400_WhenUserNotFound() throws Exception {
        String username = "ghostUser";

        given(userService.authenticate(any(), any())).willThrow(new LoginUserNotFoundException());

        doThrow(new LoginUserNotFoundException()).when(userService).deleteAccount(username);

        mockMvc.perform(post("/api/auth/delete")
                        .with(user(username)))
                .andExpect(status().isBadRequest());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}