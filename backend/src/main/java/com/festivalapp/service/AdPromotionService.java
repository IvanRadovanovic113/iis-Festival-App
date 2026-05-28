package com.festivalapp.service;

import com.festivalapp.dto.AdPromotionRequest;
import com.festivalapp.dto.AdResponse;
import com.festivalapp.model.Ad;
import com.festivalapp.model.AdPromotion;
import com.festivalapp.model.NotificationType;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdPromotionRepository;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdPromotionService {

    private final AdRepository adRepository;
    private final AdPromotionRepository adPromotionRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final WorkflowMailService workflowMailService;

    @Transactional
    public AdResponse savePromotion(Long festivalId, Long adId, AdPromotionRequest request, User user) {
        requireDirector(user);

        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to the selected festival");
        }
        if (!"PUBLISHED".equalsIgnoreCase(ad.getCurrentPhase().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only published ads can be configured for promotion");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion end date must be after start date");
        }

        AdPromotion promotion = adPromotionRepository.findByAd_AdId(adId)
            .orElse(AdPromotion.builder().ad(ad).build());

        boolean changedWindow = promotion.getStartDate() == null
            || !promotion.getStartDate().equals(request.getStartDate())
            || promotion.getEndDate() == null
            || !promotion.getEndDate().equals(request.getEndDate());

        promotion.setChannel(request.getChannel());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setPricePerDay(request.getPricePerDay());
        if (changedWindow) {
            promotion.setReminderSentAt(null);
        }

        AdPromotion savedPromotion = adPromotionRepository.save(promotion);
        ad.setPromotion(savedPromotion);
        ad.setLastEditedByUser(user);
        ad.setLastEditedRole(Role.FESTIVAL_DIRECTOR);
        ad.setLastEditedAt(LocalDateTime.now());
        ad.setLastChangeDate(LocalDate.now());
        adRepository.save(ad);

        notifyManager(ad, savedPromotion);
        return AdResponse.from(ad);
    }

    private void requireDirector(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.FESTIVAL_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only festival directors can manage promotion settings");
        }
    }

    private void notifyManager(Ad ad, AdPromotion promotion) {
        if (ad.getCampaign().getManagerUser() == null) {
            return;
        }

        Set<User> recipients = Set.of(ad.getCampaign().getManagerUser());
        String title = "Ad promotion configured";
        String message = String.format(
            Locale.ROOT,
            "Ad \"%s\" is scheduled for %s promotion from %s to %s at %s per day.",
            ad.getName(),
            promotion.getChannel().name(),
            promotion.getStartDate(),
            promotion.getEndDate(),
            promotion.getPricePerDay()
        );
        notificationService.createForRecipients(recipients, ad, NotificationType.AD_APPROVED, title, message);
        workflowMailService.send(recipients, title, message);
    }
}
