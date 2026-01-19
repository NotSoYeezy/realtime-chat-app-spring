package com.chat.realtimechat.controller.google;

import com.chat.realtimechat.exception.auth.UserNotFoundException;
import com.chat.realtimechat.exception.google.AccountNotConnectedException;
import com.chat.realtimechat.exception.google.ConnectionExpiredException;
import com.chat.realtimechat.exception.google.NoAuthenticationException;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.repository.google.GoogleRefreshTokenRepository;
import com.chat.realtimechat.repository.users.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
    public boolean checkStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        var tokenEntity = googleRefreshTokenRepository.findByUserId(user.get().getId())
                .orElseThrow(AccountNotConnectedException::new);

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
                user.get().setProviderId(null);
                userRepository.save(user.get());
                throw new ConnectionExpiredException();
            }
            if (e.getMessage().contains("insufficient authentication scopes")) {
                throw new NoAuthenticationException();
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google Calendar Check Failed", e);
        }
    }



    @GetMapping("/check-calendar")
    public boolean checkCalendar(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return googleRefreshTokenRepository.findByUserId(user.getId()).isPresent();
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