package com.festivalapp.service;

import com.festivalapp.model.AdPromotion;
import com.festivalapp.model.NotificationType;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.repository.AdPromotionRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromotionReminderService {

    private final AdPromotionRepository adPromotionRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final WorkflowMailService workflowMailService;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendUpcomingEndReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(2);
        for (AdPromotion promotion : adPromotionRepository.findAllByEndDateAndReminderSentAtIsNull(reminderDate)) {
            Set<User> directors = notificationService.uniqueRecipients(
                assignmentRepository.findAllByRole(Role.FESTIVAL_DIRECTOR).stream()
                    .map(assignment -> assignment.getUser())
                    .toList()
            );
            if (directors.isEmpty()) {
                continue;
            }

            String title = "Promotion is ending soon";
            String message = String.format(
                Locale.ROOT,
                "Promotion for ad \"%s\" on %s ends on %s. Open the ad to prolong the promotion.",
                promotion.getAd().getName(),
                promotion.getChannel().name(),
                promotion.getEndDate()
            );
            notificationService.createForRecipients(directors, promotion.getAd(), NotificationType.PROMOTION_ENDING_SOON, title, message);
            workflowMailService.send(directors, title, message);
            promotion.setReminderSentAt(LocalDateTime.now());
            adPromotionRepository.save(promotion);
        }
    }
}
