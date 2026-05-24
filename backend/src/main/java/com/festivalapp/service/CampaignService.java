package com.festivalapp.service;

import com.festivalapp.dto.CampaignRequest;
import com.festivalapp.dto.CampaignManagerOptionResponse;
import com.festivalapp.dto.CampaignResponse;
import com.festivalapp.dto.FestivalCampaignOverviewResponse;
import com.festivalapp.model.Campaign;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.CampaignRepository;
import com.festivalapp.repository.FestivalRepository;
import com.festivalapp.repository.UserRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

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

    public List<CampaignManagerOptionResponse> getFestivalManagers(Long festivalId, User user) {
        requireFestivalDirector(user);
        if (!festivalRepository.existsById(festivalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found");
        }
        return assignmentRepository.findAllByFestival_FestivalIdAndRole(festivalId, Role.FESTIVAL_MANAGER).stream()
            .map(UserFestivalAssignment::getUser)
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
        if (!managerAssignment.getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager must belong to the selected festival");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (request.getStartDate().isBefore(festival.getStartDate()) || request.getEndDate().isAfter(festival.getEndDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Campaign dates must stay within the selected festival dates"
            );
        }

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
}
