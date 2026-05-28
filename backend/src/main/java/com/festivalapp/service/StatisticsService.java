package com.festivalapp.service;

import com.festivalapp.dto.StatisticsAdTypeOptionResponse;
import com.festivalapp.dto.StatisticsCampaignOptionResponse;
import com.festivalapp.dto.StatisticsPhaseCountResponse;
import com.festivalapp.dto.StatisticsResponse;
import com.festivalapp.dto.StatisticsTypeCountResponse;
import com.festivalapp.model.Ad;
import com.festivalapp.model.AdType;
import com.festivalapp.model.Campaign;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdTypeRepository;
import com.festivalapp.repository.CampaignRepository;
import com.festivalapp.repository.StatisticsQueryRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CampaignRepository campaignRepository;
    private final AdTypeRepository adTypeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final StatisticsQueryRepository statisticsQueryRepository;

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
        long totalAds = statisticsQueryRepository.getTotalAds(campaignId, dateFrom, dateTo, adTypeId);
        List<StatisticsPhaseCountResponse> phaseCounts = statisticsQueryRepository.getPhaseCounts(campaignId, dateFrom, dateTo, adTypeId);
        List<StatisticsTypeCountResponse> typeCounts = statisticsQueryRepository.getTypeCounts(campaignId, dateFrom, dateTo, adTypeId);

        return new StatisticsResponse(
            campaigns.stream().map(StatisticsCampaignOptionResponse::from).toList(),
            adTypes.stream().map(StatisticsAdTypeOptionResponse::from).toList(),
            totalAds,
            phaseCounts,
            typeCounts
        );
    }
}
