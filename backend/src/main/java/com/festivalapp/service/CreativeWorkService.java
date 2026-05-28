package com.festivalapp.service;

import com.festivalapp.dto.CreativeCampaignResponse;
import com.festivalapp.dto.AdResponse;
import com.festivalapp.dto.CreativeAdUpdateRequest;
import com.festivalapp.model.Ad;
import com.festivalapp.model.AdPhase;
import com.festivalapp.model.Campaign;
import com.festivalapp.model.NotificationType;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.CampaignRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreativeWorkService {

    private final AdRepository adRepository;
    private final CampaignRepository campaignRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final AdVersionSnapshotService adVersionSnapshotService;
    private final NotificationService notificationService;
    private final WorkflowMailService workflowMailService;

    private UserFestivalAssignment requireCreativeAssignment(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.PRODUCT_DESIGNER && assignment.getRole() != Role.TECHNICAL_SUPPORT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only product designers and technical support can edit ad content");
        }
        return assignment;
    }

    @Transactional(readOnly = true)
    public List<CreativeCampaignResponse> getCampaigns(User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        return campaignRepository.findAllByOrderByStartDateAsc().stream()
            .map(campaign -> CreativeCampaignResponse.from(campaign, getEligibleAds(campaign, assignment.getRole()).size()))
            .filter(campaign -> campaign.getEligibleAds() > 0)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdResponse> getCampaignAds(Long campaignId, User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        return getEligibleAds(campaign, assignment.getRole()).stream()
            .map(AdResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdResponse getAd(Long campaignId, Long adId, User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        ensureAdAccess(ad, assignment.getRole());
        return AdResponse.from(ad);
    }

    @Transactional
    public AdResponse updateAd(Long campaignId, Long adId, CreativeAdUpdateRequest request, User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        ensureAdAccess(ad, assignment.getRole());

        String nextContentValue = request.getContentValue().trim();

        boolean changed = !ad.getContentFileName().equals(nextContentValue);
        if (!changed) {
            return AdResponse.from(ad);
        }

        ad.setContentFileName(nextContentValue);
        ad.setLastChangeDate(LocalDate.now());
        ad.setVersionNumber(ad.getVersionNumber() + 1);
        ad.setLastEditedByUser(user);
        ad.setLastEditedPhase(ad.getCurrentPhase());
        ad.setLastEditedRole(assignment.getRole());
        ad.setLastEditedAt(LocalDateTime.now());
        AdPhase previousPhase = ad.getCurrentPhase();
        AdPhase nextPhase = resolveNextPhase(ad);
        ad.setCurrentPhase(nextPhase);
        ad.setRejectionReason(null);
        Ad savedAd = adRepository.save(ad);
        adVersionSnapshotService.captureSnapshot(savedAd);
        notifyNextRole(savedAd, previousPhase, nextPhase);
        return AdResponse.from(savedAd);
    }

    private AdPhase resolveNextPhase(Ad ad) {
        List<AdPhase> phases = ad.getAdType().getPhases();
        for (int index = 0; index < phases.size(); index++) {
            if (phases.get(index).getPhaseId().equals(ad.getCurrentPhase().getPhaseId())) {
                if (index + 1 >= phases.size()) {
                    return phases.get(index);
                }
                return phases.get(index + 1);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current phase is not part of the ad type workflow");
    }

    private void notifyNextRole(Ad ad, AdPhase previousPhase, AdPhase nextPhase) {
        if (nextPhase.getPhaseId().equals(previousPhase.getPhaseId())) {
            return;
        }

        var uniqueRecipients = resolvePhaseRecipients(ad, nextPhase.getAssignedRole());
        String title = "Ad moved to the next phase";
        String message = String.format(
            Locale.ROOT,
            "Ad \"%s\" was completed in phase \"%s\" and moved to \"%s\".",
            ad.getName(),
            previousPhase.getName(),
            nextPhase.getName()
        );

        notificationService.createForRecipients(uniqueRecipients, ad, NotificationType.PHASE_ASSIGNED, title, message);
        if (nextPhase.isEmailNotification()) {
            workflowMailService.send(uniqueRecipients, title, message);
        }
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

    private List<Ad> getEligibleAds(Campaign campaign, Role role) {
        return adRepository.findAllByCampaign_CampaignIdAndCurrentPhase_AssignedRoleOrderByLastChangeDateDescAdIdDesc(campaign.getCampaignId(), role).stream()
            .filter(ad -> isAllowedContentType(role, ad.getAdType().getContentType()))
            .toList();
    }

    private boolean isAllowedContentType(Role role, String contentType) {
        Set<String> designerTypes = Set.of("Text", "Image");
        Set<String> supportTypes = Set.of("Audio", "Video");
        return switch (role) {
            case PRODUCT_DESIGNER -> designerTypes.contains(contentType);
            case TECHNICAL_SUPPORT -> supportTypes.contains(contentType);
            default -> false;
        };
    }

    private void ensureAdAccess(Ad ad, Role role) {
        if (ad.getCurrentPhase().getAssignedRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This ad is not currently assigned to your role");
        }
        if (!isAllowedContentType(role, ad.getAdType().getContentType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This ad content type is not assigned to your role");
        }
    }
}
