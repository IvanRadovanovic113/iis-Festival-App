package com.festivalapp.service;

import com.festivalapp.dto.AdResponse;
import com.festivalapp.model.*;
import com.festivalapp.repository.AdPhaseRepository;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdWorkflowService {

    private final AdRepository adRepository;
    private final AdPhaseRepository adPhaseRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final WorkflowMailService workflowMailService;

    @Transactional
    public AdResponse approve(Long festivalId, Long adId, Role actorRole, User user) {
        Ad ad = requireReviewableAd(festivalId, adId, actorRole, user);
        AdPhase previousPhase = ad.getCurrentPhase();
        AdPhase nextPhase = resolveNextApprovalPhase(ad, actorRole);

        ad.setCurrentPhase(nextPhase);
        ad.setLastChangeDate(LocalDate.now());
        ad.setRejectionReason(null);
        Ad savedAd = adRepository.save(ad);

        Set<User> uniqueRecipients = resolvePhaseRecipients(savedAd, nextPhase.getAssignedRole());

        String title = "Ad moved to the next phase";
        String message = String.format(
            Locale.ROOT,
            "Ad \"%s\" was approved in phase \"%s\" and moved to \"%s\".",
            savedAd.getName(),
            previousPhase.getName(),
            nextPhase.getName()
        );

        notificationService.createForRecipients(uniqueRecipients, savedAd, NotificationType.AD_APPROVED, title, message);
        if (nextPhase.isEmailNotification()) {
            workflowMailService.send(uniqueRecipients, title, message);
        }
        notifyManagerAfterDirectorApproval(savedAd, actorRole, previousPhase, nextPhase);

        return AdResponse.from(savedAd);
    }

    @Transactional
    public AdResponse reject(Long festivalId, Long adId, String reason, Role actorRole, User user) {
        Ad ad = requireReviewableAd(festivalId, adId, actorRole, user);
        AdPhase rejectedPhase = ad.getCurrentPhase();
        AdPhase previousPhase = resolveAdjacentPhase(ad, -1);

        ad.setCurrentPhase(previousPhase);
        ad.setLastChangeDate(LocalDate.now());
        ad.setRejectionReason(reason.trim());
        Ad savedAd = adRepository.save(ad);

        Set<User> recipients = resolveRejectRecipients(savedAd, previousPhase);
        String title = "Ad was rejected";
        String message = String.format(
            Locale.ROOT,
            "Ad \"%s\" was rejected in phase \"%s\" and returned to \"%s\". Reason: %s",
            savedAd.getName(),
            rejectedPhase.getName(),
            previousPhase.getName(),
            reason.trim()
        );

        notificationService.createForRecipients(recipients, savedAd, NotificationType.AD_REJECTED, title, message);
        if (previousPhase.isEmailNotification()) {
            workflowMailService.send(recipients, title, message);
        }

        return AdResponse.from(savedAd);
    }

    private Ad requireReviewableAd(Long festivalId, Long adId, Role actorRole, User user) {
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to your festival");
        }

        if (actorRole == Role.FESTIVAL_MANAGER) {
            UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
            if (assignment.getRole() != Role.FESTIVAL_MANAGER || ad.getCampaign().getManagerUser() == null
                || !ad.getCampaign().getManagerUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This workflow action is not available for the current user");
            }
        } else if (actorRole == Role.FESTIVAL_DIRECTOR) {
            UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
            if (assignment.getRole() != Role.FESTIVAL_DIRECTOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This workflow action is not available for the current user");
            }
        } else {
            UserFestivalAssignment assignment = assignmentRepository.findByUser_IdAndFestival_FestivalId(user.getId(), festivalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
            if (assignment.getRole() != actorRole) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This workflow action is not available for the current user");
            }
        }

        if (ad.getCurrentPhase().getAssignedRole() != actorRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current phase is not assigned to your role");
        }

        return ad;
    }

    private AdPhase resolveNextApprovalPhase(Ad ad, Role actorRole) {
        try {
            return resolveAdjacentPhase(ad, 1);
        } catch (ResponseStatusException ex) {
            if (actorRole == Role.FESTIVAL_DIRECTOR && ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return adPhaseRepository.findByNameIgnoreCase("PUBLISHED")
                    .orElseThrow(() -> ex);
            }
            throw ex;
        }
    }

    private AdPhase resolveAdjacentPhase(Ad ad, int direction) {
        List<AdPhase> phases = ad.getAdType().getPhases();
        int currentIndex = -1;
        for (int index = 0; index < phases.size(); index++) {
            if (phases.get(index).getPhaseId().equals(ad.getCurrentPhase().getPhaseId())) {
                currentIndex = index;
                break;
            }
        }

        if (currentIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current phase is not part of the ad type workflow");
        }

        int targetIndex = currentIndex + direction;
        if (targetIndex < 0 || targetIndex >= phases.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, direction > 0
                ? "The ad is already in the final phase"
                : "The ad is already in the first phase");
        }

        return phases.get(targetIndex);
    }

    private Set<User> resolveRejectRecipients(Ad ad, AdPhase previousPhase) {
        if (ad.getLastEditedByUser() != null
            && ad.getLastEditedPhase() != null
            && ad.getLastEditedPhase().getPhaseId().equals(previousPhase.getPhaseId())
            && ad.getLastEditedRole() == previousPhase.getAssignedRole()) {
            return Set.of(ad.getLastEditedByUser());
        }

        return resolvePhaseRecipients(ad, previousPhase.getAssignedRole());
    }

    private Set<User> resolvePhaseRecipients(Ad ad, Role role) {
        if (role == Role.FESTIVAL_MANAGER && ad.getCampaign().getManagerUser() != null) {
            return Set.of(ad.getCampaign().getManagerUser());
        }

        return notificationService.uniqueRecipients(
            assignmentRepository.findAllByRole(role).stream()
                .map(UserFestivalAssignment::getUser)
                .toList()
        );
    }

    private void notifyManagerAfterDirectorApproval(Ad ad, Role actorRole, AdPhase previousPhase, AdPhase nextPhase) {
        if (actorRole != Role.FESTIVAL_DIRECTOR || ad.getCampaign().getManagerUser() == null) {
            return;
        }

        User manager = ad.getCampaign().getManagerUser();
        String title = "Director approved the ad";
        String message = String.format(
            Locale.ROOT,
            "Director approved ad \"%s\" and moved it from \"%s\" to \"%s\".",
            ad.getName(),
            previousPhase.getName(),
            nextPhase.getName()
        );

        notificationService.createForRecipients(Set.of(manager), ad, NotificationType.AD_APPROVED, title, message);
        workflowMailService.send(Set.of(manager), title, message);
    }
}
