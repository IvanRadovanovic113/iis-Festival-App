package com.festivalapp.service.eventorganization;

import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.Stage;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.repository.StageRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.eventorganization.EventReservationRequestRepository;
import com.festivalapp.repository.eventorganization.EventResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EventOrganizationAccessService {

    private final StageRepository stageRepository;
    private final EventResourceRepository eventResourceRepository;
    private final EventReservationRequestRepository reservationRequestRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    public Festival requireEventOrganizerFestival(User user) {
        if (user.getRole() == Role.ADMIN) {
            return null;
        }
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.EVENT_ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Event organizer role is required");
        }
        return assignment.getFestival();
    }

    public Stage requireStage(Long stageId, Festival festival) {
        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage was not found"));
        if (festival != null && !stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        return stage;
    }

    public EventResource requireResource(Long resourceId, Festival festival) {
        EventResource resource = eventResourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource was not found"));
        if (festival != null && !resource.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resource does not belong to your festival");
        }
        return resource;
    }

    public EventReservationRequest requireReservationRequest(Long requestId, Festival festival) {
        EventReservationRequest request = reservationRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation request was not found"));
        if (festival != null && !request.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation request does not belong to your festival");
        }
        return request;
    }
}
