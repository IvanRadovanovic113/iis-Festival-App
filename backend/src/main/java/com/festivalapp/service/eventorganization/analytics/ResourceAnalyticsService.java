package com.festivalapp.service.eventorganization.analytics;

import com.festivalapp.dto.eventorganization.analytics.ResourceAnalyticsResponse;
import com.festivalapp.dto.eventorganization.analytics.ResourceStageOccupancyResponse;
import com.festivalapp.dto.eventorganization.analytics.ResourceTopResourceResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.repository.eventorganization.analytics.ResourceAnalyticsQueryRepository;
import com.festivalapp.service.eventorganization.EventOrganizationAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceAnalyticsService {

    private final ResourceAnalyticsQueryRepository analyticsRepository;
    private final EventOrganizationAccessService accessService;

    public ResourceAnalyticsResponse getAnalytics(User user, Integer year, Integer month, Long stageId) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        if (festival == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Analytics require a festival assignment");
        }

        Long festivalId = festival.getFestivalId();

        ResourceAnalyticsResponse summary = analyticsRepository.getSummary(festivalId, year, month, stageId);
        List<ResourceTopResourceResponse> topResources = analyticsRepository.getTopResources(festivalId, year, month, stageId);
        List<ResourceStageOccupancyResponse> stageOccupancies = analyticsRepository.getStageOccupancy(festivalId, year, month);

        return new ResourceAnalyticsResponse(
            summary.getTotalReservations(),
            summary.getMostUsedResourceName(),
            summary.getMostUsedResourceCount(),
            summary.getAvgStageOccupancy(),
            summary.getExtraResourceRequests(),
            topResources,
            stageOccupancies
        );
    }
}
