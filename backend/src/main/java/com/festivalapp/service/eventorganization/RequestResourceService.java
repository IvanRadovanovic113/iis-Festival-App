package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.RequestResourceResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestResourceService {

    private final RequestResourceRepository requestResourceRepository;
    private final EventOrganizationAccessService accessService;

    public List<RequestResourceResponse> getRequestResources(Long requestId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireReservationRequest(requestId, festival);
        return requestResourceRepository.findByReservationRequest_IdOrderByResource_NameAsc(requestId)
            .stream().map(RequestResourceResponse::from).toList();
    }
}
