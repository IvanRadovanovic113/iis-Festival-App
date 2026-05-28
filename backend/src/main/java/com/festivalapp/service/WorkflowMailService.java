package com.festivalapp.service;

import com.festivalapp.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowMailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String fromAddress;

    public void send(Collection<User> recipients, String subject, String body) {
        if (!mailEnabled) {
            log.info("Mail delivery skipped because app.mail.enabled=false. Subject: {}", subject);
            return;
        }

        for (User recipient : recipients) {
            if (recipient.getEmail() == null || recipient.getEmail().isBlank()) {
                continue;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                if (fromAddress != null && !fromAddress.isBlank()) {
                    message.setFrom(fromAddress);
                }
                message.setTo(recipient.getEmail());
                message.setSubject(subject);
                message.setText(body);
                javaMailSender.send(message);
            } catch (Exception exception) {
                log.warn("Failed to send workflow mail to {}", recipient.getEmail(), exception);
            }
        }
    }
}
