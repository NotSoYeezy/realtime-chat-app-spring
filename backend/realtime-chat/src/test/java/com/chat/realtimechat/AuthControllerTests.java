package com.chat.realtimechat;

import com.chat.realtimechat.config.SecurityConfig; // 1. IMPORT YOUR CONFIG
import com.chat.realtimechat.controller.security.AuthController;
import com.chat.realtimechat.model.dto.request.LoginRequest;
import com.chat.realtimechat.model.dto.request.RegistrationRequest;
import com.chat.realtimechat.model.dto.request.UpdateUserRequest;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.entity.auth.RefreshToken;
import com.chat.realtimechat.model.entity.auth.RegisterConfirmToken;
import com.chat.realtimechat.repository.RefreshTokenRepository;
import com.chat.realtimechat.repository.RegisterConfirmTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.security.JwtAuthFilter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.Optional;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTests {

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
            chain.doFilter(request, response); // Continue the chain!
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }


    @Test
    public void login_ShouldReturnTokens_WhenCredentialsAreCorrect() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("bob@test.com");
        loginReq.setPassword("password123");

        User mockUser = new User();
        mockUser.setId(2L);
        mockUser.setEmail("bob@test.com");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("refresh-token-xyz");

        given(userService.authenticate("bob@test.com", "password123")).willReturn(mockUser);
        given(jwtUtil.generateToken(mockUser)).willReturn("fake-jwt-token");
        given(refreshTokenService.createRefreshToken(2L)).willReturn(mockRefreshToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-xyz"));
    }

    @Test
    public void register_ShouldReturn201_WhenRequestIsValid() throws Exception {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("alice@test.com");
        req.setPassword("SecurePass123!");
        req.setName("Alice");
        req.setSurname("Smith");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("alice@test.com");
        mockUser.setName("Alice");
        mockUser.setSurname("Smith");

        RegisterConfirmToken mockToken = new RegisterConfirmToken();
        mockToken.setToken("uuid-token-123");

        given(userService.registerUser(any(RegistrationRequest.class))).willReturn(mockUser);
        given(registerConfirmTokenService.createRegisterConfirmToken(anyLong())).willReturn(mockToken);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    public void update_ShouldReturn200_WhenRequestIsValid() throws Exception {
        UpdateUserRequest updateReq = new UpdateUserRequest();
        updateReq.setName("NewName");
        updateReq.setSurname("NewSurname");

        User mockUpdatedUser = new User();
        mockUpdatedUser.setUsername("testuser");

        given(userService.updateUser(any(UpdateUserRequest.class), eq("testuser")))
                .willReturn(mockUpdatedUser);

        mockMvc.perform(post("/api/auth/update")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(content().string("You changed your credentials"));
    }

    @Test
    public void refreshToken_ShouldReturnNewAccessToken_WhenRefreshTokenIsValid() throws Exception {
        String validRefreshToken = "valid-refresh-token";
        String newJwt = "new-jwt-token";

        String requestBody = "{\"refreshToken\": \"" + validRefreshToken + "\"}";

        given(refreshTokenService.generateNewJwtToken(validRefreshToken)).willReturn(newJwt);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(newJwt))
                .andExpect(jsonPath("$.refreshToken").value(validRefreshToken));
    }


    @Test
    public void logout_ShouldReturn200_WhenRefreshTokenIsValid() throws Exception {
        String tokenString = "valid-token-to-delete";
        String requestBody = "{\"refreshToken\": \"" + tokenString + "\"}";

        RefreshToken mockTokenEntity = new RefreshToken();
        mockTokenEntity.setToken(tokenString);

        given(refreshTokenRepository.findByToken(tokenString)).willReturn(Optional.of(mockTokenEntity));

        mockMvc.perform(post("/api/auth/logout")
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully."));

        verify(refreshTokenRepository).delete(mockTokenEntity);
    }


    @Test
    public void getMe_ShouldReturnUserId_WhenUserIsAuthenticated() throws Exception {
        User mockUser = new User();
        mockUser.setId(123L);
        mockUser.setUsername("testuser");

        given(userService.findUsersByUsername("testuser")).willReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/auth/me")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().string("123"));
    }


    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}