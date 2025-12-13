package com.chat.realtimechat.security;

import com.chat.realtimechat.model.dto.response.RegisteredUserResponse;
import com.chat.realtimechat.model.entity.RefreshToken;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.security.RefreshTokenService;
import com.chat.realtimechat.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String id = oAuth2User.getAttribute("sub");
        User user;

        if(userRepository.findByProviderId(id).isPresent()){
            user = userRepository.findByProviderId(id).get();
        }
        else if(userRepository.findByEmail(email).isPresent()){
            user = userRepository.findByEmail(email).get();
            user.setProviderId(id);
        }
        else{
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setName(name.split(" ")[0]);
            newUser.setSurname(name.split(" ")[1]);
            newUser.setProviderId(id);
            user = userRepository.save(newUser);
        }

        String token = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        String targetUrl = frontendUrl + "/auth/callback"
                +"?token=" + token
                +"&refreshToken=" + refreshToken.getToken();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
