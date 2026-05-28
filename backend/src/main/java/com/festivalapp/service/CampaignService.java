package com.festivalapp.service;

import com.festivalapp.dto.CampaignRequest;
import com.festivalapp.dto.CampaignManagerOptionResponse;
import com.festivalapp.dto.CampaignResponse;
import com.festivalapp.dto.CampaignStatsResponse;
import com.festivalapp.dto.CampaignWorkspaceResponse;
import com.festivalapp.dto.FestivalCampaignOverviewResponse;
import com.festivalapp.dto.AdResponse;
import com.festivalapp.model.Campaign;
import com.festivalapp.model.Ad;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.AdNotificationRepository;
import com.festivalapp.repository.AdPromotionRepository;
import com.festivalapp.repository.AdVersionRepository;
import com.festivalapp.repository.CampaignRepository;
import com.festivalapp.repository.FestivalRepository;
import com.festivalapp.repository.UserRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final AdRepository adRepository;
    private final AdVersionRepository adVersionRepository;
    private final AdNotificationRepository adNotificationRepository;
    private final AdPromotionRepository adPromotionRepository;

    private void requireFestivalDirector(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.FESTIVAL_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only festival directors can manage campaigns");
        }
    }

    public List<FestivalCampaignOverviewResponse> getFestivalOverviews(User user) {
        requireFestivalDirector(user);
        Map<Long, Campaign> campaignsByFestival = campaignRepository.findAll().stream()
            .collect(Collectors.toMap(c -> c.getFestival().getFestivalId(), Function.identity()));

        return festivalRepository.findAll().stream()
            .map(festival -> FestivalCampaignOverviewResponse.from(festival, campaignsByFestival.get(festival.getFestivalId())))
            .toList();
    }

    public CampaignResponse getCampaignByFestival(Long festivalId, User user) {
        requireFestivalDirector(user);
        Campaign campaign = campaignRepository.findByFestival_FestivalId(festivalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        return CampaignResponse.from(campaign);
    }

    public CampaignWorkspaceResponse getCampaignWorkspace(Long festivalId, User user) {
        requireFestivalDirector(user);
        Campaign campaign = campaignRepository.findByFestival_FestivalId(festivalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        List<Ad> ads = adRepository.findAllByCampaign_CampaignIdOrderByLastChangeDateDescAdIdDesc(campaign.getCampaignId());
        return new CampaignWorkspaceResponse(CampaignResponse.from(campaign), buildStats(ads), ads.stream().map(AdResponse::from).toList());
    }

    public List<CampaignManagerOptionResponse> getFestivalManagers(Long festivalId, User user) {
        requireFestivalDirector(user);
        if (!festivalRepository.existsById(festivalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found");
        }
        return assignmentRepository.findAllByRole(Role.FESTIVAL_MANAGER).stream()
            .map(UserFestivalAssignment::getUser)
            .collect(Collectors.toMap(
                User::getId,
                Function.identity(),
                (left, right) -> left,
                LinkedHashMap::new
            ))
            .values().stream()
            .map(CampaignManagerOptionResponse::from)
            .toList();
    }

    public CampaignResponse createCampaign(Long festivalId, CampaignRequest request, User user) {
        requireFestivalDirector(user);

        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found"));
        User managerUser = userRepository.findById(request.getManagerUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager user not found"));
        UserFestivalAssignment managerAssignment = assignmentRepository.findByUser_Id(managerUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager must be assigned to a festival"));

        if (campaignRepository.existsByFestival_FestivalId(festivalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This festival already has a campaign");
        }
        if (managerAssignment.getRole() != Role.FESTIVAL_MANAGER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not a festival manager");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (request.getEndDate().isAfter(festival.getEndDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Campaign end date must not be after the selected festival end date"
            );
        }

        managerAssignment.setFestival(festival);
        assignmentRepository.save(managerAssignment);

        Campaign campaign = Campaign.builder()
            .name(request.getName().trim())
            .description(request.getDescription().trim())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .managerUser(managerUser)
            .festival(festival)
            .build();

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignResponse updateCampaign(Long festivalId, CampaignRequest request, User user) {
        requireFestivalDirector(user);

        Campaign campaign = campaignRepository.findByFestival_FestivalId(festivalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        Festival festival = campaign.getFestival();
        User managerUser = userRepository.findById(request.getManagerUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager user not found"));
        UserFestivalAssignment managerAssignment = assignmentRepository.findByUser_Id(managerUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager must be assigned to a festival"));

        if (managerAssignment.getRole() != Role.FESTIVAL_MANAGER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not a festival manager");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (request.getEndDate().isAfter(festival.getEndDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Campaign end date must not be after the selected festival end date"
            );
        }

        managerAssignment.setFestival(festival);
        assignmentRepository.save(managerAssignment);

        campaign.setName(request.getName().trim());
        campaign.setDescription(request.getDescription().trim());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setManagerUser(managerUser);

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    @Transactional
    public void deleteCampaign(Long festivalId, User user) {
        requireFestivalDirector(user);
        Campaign campaign = campaignRepository.findByFestival_FestivalId(festivalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        List<Ad> ads = adRepository.findAllByCampaign_CampaignIdOrderByLastChangeDateDescAdIdDesc(campaign.getCampaignId());
        for (Ad ad : ads) {
            adNotificationRepository.deleteAllByAd_AdId(ad.getAdId());
            adVersionRepository.deleteAllByAd_AdId(ad.getAdId());
            adPromotionRepository.deleteByAd_AdId(ad.getAdId());
            adRepository.delete(ad);
        }

        campaignRepository.delete(campaign);
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
