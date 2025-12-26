package com.chat.realtimechat.controller;

import com.chat.realtimechat.exception.UserNotFoundException;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.repository.GoogleRefreshTokenRepository;
import com.chat.realtimechat.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse; // Added for exception handling
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
// FIX 1: Remove the wrong import (com.google.api.client.util.Value)
import org.springframework.beans.factory.annotation.Value; // <--- Correct Spring import
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/google/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final GoogleRefreshTokenRepository googleRefreshTokenRepository;

    @GetMapping("/check-status")
    public boolean checkStatus(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        var tokenEntity = googleRefreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Google Calendar not connected"));

        try {
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setRefreshToken(tokenEntity.getToken());

            Calendar service = new Calendar.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("realtime chat").build();

            return isUserBusyNow(service);

        } catch (Exception e) {
            if (e.getMessage().contains("invalid_grant")) {
                googleRefreshTokenRepository.delete(tokenEntity);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Google Calendar connection expired");
            }
            throw new RuntimeException("Google Calendar Check Failed", e);
        }
    }

    private boolean isUserBusyNow(Calendar service) throws Exception {
        long nowMillis = System.currentTimeMillis();
        DateTime now = new DateTime(nowMillis);
        DateTime endOfWindow = new DateTime(nowMillis + (60 * 1000));

        Events events = service.events().list("primary")
                .setTimeMin(now)
                .setTimeMax(endOfWindow)
                .setSingleEvents(true)
                .execute();

        return !events.getItems().isEmpty();
    }
}