package com.chat.realtimechat.controller.google;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/google")

public class GoogleAccountController {
    @PostMapping("/google-link")
    public void prepareGoogleLink(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response) {
        Cookie linkCookie = new Cookie("GOOGLE_LINK_INTENT", userDetails.getUsername());

        linkCookie.setHttpOnly(true);
        linkCookie.setPath("/");
        linkCookie.setMaxAge(180);

        response.addCookie(linkCookie);
    }
}
