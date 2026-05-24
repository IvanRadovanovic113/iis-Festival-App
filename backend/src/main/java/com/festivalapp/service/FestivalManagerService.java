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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalManagerService {

    private final FestivalRepository festivalRepository;
    private final CampaignRepository campaignRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final AdRepository adRepository;
    private final AdTypeRepository adTypeRepository;
    private final AdPhaseRepository adPhaseRepository;

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

    @Transactional
    public AdResponse createAd(Long campaignId, AdRequest request, User user) {
        requireFestivalManager(user);

        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        AdType adType = adTypeRepository.findById(request.getAdTypeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad type not found"));

        AdPhase initialPhase = adType.getPhases().stream()
            .min(Comparator.comparing(AdPhase::getOrderIndex))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected ad type has no phases"));

        Ad ad = Ad.builder()
            .campaign(campaign)
            .adType(adType)
            .currentPhase(initialPhase)
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .contentFileName(request.getContentFileName().trim())
            .lastChangeDate(LocalDate.now())
            .versionNumber(1)
            .build();

        return AdResponse.from(adRepository.save(ad));
    }

    @Transactional
    public AdTypeResponse createAdType(AdTypeRequest request, User user) {
        requireFestivalManager(user);
        if (adTypeRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ad type with this name already exists");
        }

        List<AdPhase> phases = adPhaseRepository.findAllById(request.getPhaseIds());
        if (phases.size() != request.getPhaseIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more phases were not found");
        }

        AdType adType = AdType.builder()
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .contentType(request.getContentType().trim())
            .phases(phases.stream()
                .sorted(Comparator.comparing(AdPhase::getOrderIndex))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new)))
            .build();

        return AdTypeResponse.from(adTypeRepository.save(adType));
    }

    @Transactional
    public AdPhaseResponse createPhase(AdPhaseRequest request, User user) {
        requireFestivalManager(user);
        if (adPhaseRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ad phase with this name already exists");
        }
        AdType adType = adTypeRepository.findById(request.getAdTypeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad type not found"));

        AdPhase phase = AdPhase.builder()
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .orderIndex(request.getOrderIndex())
            .emailNotification(Boolean.TRUE.equals(request.getEmailNotification()))
            .build();
        AdPhase savedPhase = adPhaseRepository.save(phase);
        adType.getPhases().add(savedPhase);
        adTypeRepository.save(adType);
        return AdPhaseResponse.from(savedPhase);
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
}
