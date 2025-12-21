package com.chat.realtimechat.security;

import com.chat.realtimechat.model.entity.auth.RefreshToken;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.service.UserService;
import com.chat.realtimechat.service.security.RefreshTokenService;
import com.chat.realtimechat.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = userService.registeredGoogleUser(oAuth2User);

        String token = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        String targetUrl = frontendUrl + "/auth/callback"
                +"?token=" + token
                +"&refreshToken=" + refreshToken.getToken();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
