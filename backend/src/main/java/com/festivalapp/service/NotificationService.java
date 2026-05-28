package com.festivalapp.service;

import com.festivalapp.dto.NotificationResponse;
import com.festivalapp.model.Ad;
import com.festivalapp.model.AdNotification;
import com.festivalapp.model.NotificationType;
import com.festivalapp.model.User;
import com.festivalapp.repository.AdNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AdNotificationRepository notificationRepository;

    @Transactional
    public void createForRecipients(Set<User> recipients, Ad ad, NotificationType type, String title, String message) {
        for (User recipient : recipients) {
            notificationRepository.save(AdNotification.builder()
                .recipientUser(recipient)
                .ad(ad)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(User user) {
        return notificationRepository.findAllByRecipientUser_IdOrderByCreatedAtDescNotificationIdDesc(user.getId()).stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User user) {
        AdNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getRecipientUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notification does not belong to the current user");
        }
        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    public Set<User> uniqueRecipients(List<User> recipients) {
        return new LinkedHashSet<>(recipients);
    }
}
