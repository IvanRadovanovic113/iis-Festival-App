package com.festivalapp.service;

import com.festivalapp.dto.*;
import com.festivalapp.model.*;
import com.festivalapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalManagerService {
    private static final List<Role> PHASE_ASSIGNABLE_ROLES = List.of(
        Role.PRODUCT_DESIGNER,
        Role.TECHNICAL_SUPPORT,
        Role.FESTIVAL_MANAGER,
        Role.FESTIVAL_DIRECTOR
    );

    private final FestivalRepository festivalRepository;
    private final CampaignRepository campaignRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final AdRepository adRepository;
    private final AdTypeRepository adTypeRepository;
    private final AdPhaseRepository adPhaseRepository;
    private final AdVersionSnapshotService adVersionSnapshotService;

    private void requireFestivalManager(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.FESTIVAL_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only festival managers can manage ads");
        }
    }

    public List<FestivalCampaignOverviewResponse> getFestivalOverviews(User user) {
        requireFestivalManager(user);
        var campaignsByFestival = campaignRepository.findAll().stream()
            .collect(Collectors.toMap(c -> c.getFestival().getFestivalId(), Function.identity()));

        return festivalRepository.findAll().stream()
            .map(festival -> FestivalCampaignOverviewResponse.from(festival, campaignsByFestival.get(festival.getFestivalId())))
            .toList();
    }

    public CampaignWorkspaceResponse getCampaignWorkspace(Long festivalId, User user) {
        requireFestivalManager(user);
        Campaign campaign = campaignRepository.findByFestival_FestivalId(festivalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        List<Ad> ads = adRepository.findAllByCampaign_CampaignIdOrderByLastChangeDateDescAdIdDesc(campaign.getCampaignId());
        return new CampaignWorkspaceResponse(CampaignResponse.from(campaign), buildStats(ads), ads.stream().map(AdResponse::from).toList());
    }

    public List<AdTypeResponse> getAdTypes(User user) {
        requireFestivalManager(user);
        return adTypeRepository.findAllByOrderByNameAsc().stream()
            .map(AdTypeResponse::from)
            .toList();
    }

    public List<AdPhaseResponse> getPhases(User user) {
        requireFestivalManager(user);
        return adPhaseRepository.findAllByOrderByOrderIndexAscNameAsc().stream()
            .map(AdPhaseResponse::from)
            .toList();
    }

    public AdResponse getAd(Long festivalId, Long adId, User user) {
        requireFestivalManager(user);
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to your festival");
        }
        return AdResponse.from(ad);
    }

    @Transactional
    public AdResponse createAd(Long campaignId, AdRequest request, User user) {
        requireFestivalManager(user);

        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        AdType adType = adTypeRepository.findById(request.getAdTypeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad type not found"));

        AdPhase initialPhase = adType.getPhases().stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected ad type has no phases"));

        Ad ad = Ad.builder()
            .campaign(campaign)
            .adType(adType)
            .currentPhase(initialPhase)
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .contentFileName("")
            .lastChangeDate(LocalDate.now())
            .versionNumber(1)
            .lastEditedByUser(user)
            .lastEditedPhase(initialPhase)
            .lastEditedRole(Role.FESTIVAL_MANAGER)
            .lastEditedAt(LocalDateTime.now())
            .build();

        Ad savedAd = adRepository.save(ad);
        adVersionSnapshotService.captureSnapshot(savedAd);
        return AdResponse.from(savedAd);
    }

    @Transactional
    public AdResponse updateAd(Long festivalId, Long adId, ManagerAdUpdateRequest request, User user) {
        requireFestivalManager(user);
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to your festival");
        }
        if (isPublished(ad)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Published ads can no longer be edited by the festival manager");
        }

        String nextName = request.getName().trim();
        String nextDescription = request.getDescription().trim();
        boolean changed = !ad.getName().equals(nextName) || !ad.getDescription().equals(nextDescription);
        if (!changed) {
            return AdResponse.from(ad);
        }

        ad.setName(nextName);
        ad.setDescription(nextDescription);
        ad.setLastChangeDate(LocalDate.now());
        ad.setVersionNumber(ad.getVersionNumber() + 1);
        ad.setLastEditedByUser(user);
        ad.setLastEditedPhase(ad.getCurrentPhase());
        ad.setLastEditedRole(Role.FESTIVAL_MANAGER);
        ad.setLastEditedAt(LocalDateTime.now());
        Ad savedAd = adRepository.save(ad);
        adVersionSnapshotService.captureSnapshot(savedAd);
        return AdResponse.from(savedAd);
    }

    @Transactional
    public void deleteAd(Long festivalId, Long adId, User user) {
        requireFestivalManager(user);
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to your festival");
        }
        if (isPublished(ad)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Published ads can no longer be deleted");
        }

        adRepository.delete(ad);
    }

    @Transactional
    public AdTypeResponse createAdType(AdTypeRequest request, User user) {
        requireFestivalManager(user);
        if (adTypeRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ad type with this name already exists");
        }

        var phasesById = adPhaseRepository.findAllById(request.getPhaseIds()).stream()
            .collect(Collectors.toMap(AdPhase::getPhaseId, Function.identity()));
        if (phasesById.size() != request.getPhaseIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more phases were not found");
        }
        AdPhase directorApprovalPhase = requireNamedPhase("DIRECTOR APPROVAL");
        AdPhase publishedPhase = requireNamedPhase("PUBLISHED");

        List<AdPhase> orderedPhases = request.getPhaseIds().stream()
            .map(phasesById::get)
            .filter(phase -> !phase.getPhaseId().equals(directorApprovalPhase.getPhaseId()))
            .filter(phase -> !phase.getPhaseId().equals(publishedPhase.getPhaseId()))
            .toList();

        orderedPhases = java.util.stream.Stream.concat(
                orderedPhases.stream(),
                List.of(directorApprovalPhase, publishedPhase).stream()
            )
            .toList();

        AdType adType = AdType.builder()
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .contentType(request.getContentType().trim())
            .phases(orderedPhases)
            .build();

        return AdTypeResponse.from(adTypeRepository.save(adType));
    }

    @Transactional
    public AdPhaseResponse createPhase(AdPhaseRequest request, User user) {
        requireFestivalManager(user);
        if (adPhaseRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ad phase with this name already exists");
        }
        if (!PHASE_ASSIGNABLE_ROLES.contains(request.getAssignedRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned role is not allowed for ad phases");
        }

        AdPhase phase = AdPhase.builder()
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .orderIndex(request.getOrderIndex())
            .emailNotification(Boolean.TRUE.equals(request.getEmailNotification()))
            .assignedRole(request.getAssignedRole())
            .build();
        AdPhase savedPhase = adPhaseRepository.save(phase);
        if (request.getAdTypeId() != null) {
            AdType adType = adTypeRepository.findById(request.getAdTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad type not found"));
            int insertIndex = Math.max(0, Math.min(request.getOrderIndex() - 1, adType.getPhases().size()));
            adType.getPhases().add(insertIndex, savedPhase);
            for (int i = 0; i < adType.getPhases().size(); i++) {
                adType.getPhases().get(i).setOrderIndex(i + 1);
            }
            adTypeRepository.save(adType);
        }
        return AdPhaseResponse.from(savedPhase);
    }

    private AdPhase requireNamedPhase(String phaseName) {
        return adPhaseRepository.findByNameIgnoreCase(phaseName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Required workflow phase \"" + phaseName + "\" is missing"
            ));
    }

    private CampaignStatsResponse buildStats(List<Ad> ads) {
        long draft = countByNormalizedPhaseName(ads, "DRAFT");
        long approvedTechnical = countByNormalizedPhaseName(ads, "APPROVED_TECHNICAL");
        long approved = countByNormalizedPhaseName(ads, "APPROVED");
        long rejected = ads.stream()
            .filter(ad -> normalizePhaseName(ad.getCurrentPhase().getName()).contains("REJECTED"))
            .count();
        long total = ads.size();
        long todo = total - approved - rejected;
        return new CampaignStatsResponse(total, todo, draft, approvedTechnical, approved, rejected);
    }

    private long countByNormalizedPhaseName(List<Ad> ads, String expected) {
        return ads.stream()
            .filter(ad -> normalizePhaseName(ad.getCurrentPhase().getName()).equals(expected))
            .count();
    }

    private String normalizePhaseName(String name) {
        return name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private boolean isPublished(Ad ad) {
        return normalizePhaseName(ad.getCurrentPhase().getName()).equals("PUBLISHED");
    }
}
