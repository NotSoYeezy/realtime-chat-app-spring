package com.chat.realtimechat.service.notifiers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEmailNotifierImpl implements UserEmailNotifier {

    @Value("${mail.sender}")
    private String sender;
    private final JavaMailSender mailSender;

    @Override
    public void sendMessage(String message, String topic, String to) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(sender);
        mailMessage.setTo(to);
        mailMessage.setSubject(topic);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}
