package com.festivalapp.service;

import com.festivalapp.dto.StatisticsAdTypeOptionResponse;
import com.festivalapp.dto.StatisticsCampaignOptionResponse;
import com.festivalapp.dto.StatisticsPhaseCountResponse;
import com.festivalapp.dto.StatisticsResponse;
import com.festivalapp.dto.StatisticsTypeCountResponse;
import com.festivalapp.model.Ad;
import com.festivalapp.model.AdPhase;
import com.festivalapp.model.AdType;
import com.festivalapp.model.Campaign;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdPhaseRepository;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.AdTypeRepository;
import com.festivalapp.repository.CampaignRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CampaignRepository campaignRepository;
    private final AdRepository adRepository;
    private final AdPhaseRepository adPhaseRepository;
    private final AdTypeRepository adTypeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private void requireStatisticsAccess(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.FESTIVAL_MANAGER && assignment.getRole() != Role.FESTIVAL_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only festival managers and festival directors can view statistics");
        }
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(User user, Long campaignId, LocalDate dateFrom, LocalDate dateTo, Long adTypeId) {
        requireStatisticsAccess(user);

        List<Campaign> campaigns = campaignRepository.findAll().stream()
            .sorted(Comparator.comparing(Campaign::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
        List<AdType> adTypes = adTypeRepository.findAllByOrderByNameAsc();

        List<Ad> filteredAds = adRepository.findAll().stream()
            .filter(ad -> campaignId == null || ad.getCampaign().getCampaignId().equals(campaignId))
            .filter(ad -> adTypeId == null || ad.getAdType().getAdTypeId().equals(adTypeId))
            .filter(ad -> dateFrom == null || !ad.getLastChangeDate().isBefore(dateFrom))
            .filter(ad -> dateTo == null || !ad.getLastChangeDate().isAfter(dateTo))
            .toList();

        Map<Long, Long> phaseCountsById = filteredAds.stream()
            .collect(Collectors.groupingBy(ad -> ad.getCurrentPhase().getPhaseId(), Collectors.counting()));
        List<StatisticsPhaseCountResponse> phaseCounts = adPhaseRepository.findAllByOrderByOrderIndexAscNameAsc().stream()
            .map(phase -> new StatisticsPhaseCountResponse(
                phase.getPhaseId(),
                phase.getName(),
                phase.getOrderIndex(),
                phaseCountsById.getOrDefault(phase.getPhaseId(), 0L)
            ))
            .toList();

        Map<Long, Long> typeCountsById = filteredAds.stream()
            .collect(Collectors.groupingBy(ad -> ad.getAdType().getAdTypeId(), Collectors.counting()));
        List<StatisticsTypeCountResponse> typeCounts = adTypes.stream()
            .map(type -> new StatisticsTypeCountResponse(
                type.getAdTypeId(),
                type.getName(),
                typeCountsById.getOrDefault(type.getAdTypeId(), 0L)
            ))
            .toList();

        return new StatisticsResponse(
            campaigns.stream().map(StatisticsCampaignOptionResponse::from).toList(),
            adTypes.stream().map(StatisticsAdTypeOptionResponse::from).toList(),
            filteredAds.size(),
            phaseCounts,
            typeCounts
        );
    }
}
