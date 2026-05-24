package com.festivalapp.service;

import com.festivalapp.dto.TicketTypeRequest;
import com.festivalapp.dto.TicketTypeResponse;
import com.festivalapp.model.*;
import com.festivalapp.prodaja.repository.SegmentRepository;
import com.festivalapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketTypeSegmentRepository ticketTypeSegmentRepository;
    private final SegmentRepository segmentRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireTicketTypeAccess(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_MANAGER && assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: sales manager or sales director role required");
        }
        return assignment.getFestival();
    }

    private TicketTypeResponse buildResponse(TicketType tt) {
        return TicketTypeResponse.from(tt,
            ticketTypeSegmentRepository.findByTicketType_TicketTypeId(tt.getTicketTypeId()));
    }

    public List<TicketTypeResponse> getAll(User user) {
        Festival festival = requireTicketTypeAccess(user);
        return ticketTypeRepository.findByFestival_FestivalId(festival.getFestivalId())
            .stream().map(this::buildResponse).toList();
    }

    public TicketTypeResponse getById(Long id, User user) {
        Festival festival = requireTicketTypeAccess(user);
        TicketType tt = ticketTypeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket type does not belong to your festival");
        }
        return buildResponse(tt);
    }

    @Transactional
    public TicketTypeResponse create(TicketTypeRequest request, User user) {
        Festival festival = requireTicketTypeAccess(user);
        if (ticketTypeRepository.existsByNameAndFestival_FestivalId(request.getName(), festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A ticket type with this name already exists");
        }
        TicketType tt = TicketType.builder()
            .name(request.getName())
            .totalQuantity(request.getTotalQuantity())
            .festival(festival)
            .build();
        tt = ticketTypeRepository.save(tt);
        saveSegments(tt, request.getSegmentIds(), festival.getFestivalId());
        return buildResponse(tt);
    }

    @Transactional
    public TicketTypeResponse update(Long id, TicketTypeRequest request, User user) {
        Festival festival = requireTicketTypeAccess(user);
        TicketType tt = ticketTypeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket type does not belong to your festival");
        }
        if (ticketTypeRepository.existsByNameAndFestival_FestivalIdAndTicketTypeIdNot(
                request.getName(), festival.getFestivalId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A ticket type with this name already exists");
        }
        if (request.getTotalQuantity() < tt.getSoldCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Total quantity cannot be less than already sold tickets (" + tt.getSoldCount() + ")");
        }
        tt.setName(request.getName());
        tt.setTotalQuantity(request.getTotalQuantity());
        tt = ticketTypeRepository.save(tt);

        ticketTypeSegmentRepository.deleteByTicketType_TicketTypeId(id);
        saveSegments(tt, request.getSegmentIds(), festival.getFestivalId());
        return buildResponse(tt);
    }

    @Transactional
    public void delete(Long id, User user) {
        Festival festival = requireTicketTypeAccess(user);
        TicketType tt = ticketTypeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket type does not belong to your festival");
        }
        if (tt.getSoldCount() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot delete a ticket type with " + tt.getSoldCount() + " sold tickets");
        }
        ticketTypeSegmentRepository.deleteByTicketType_TicketTypeId(id);
        ticketTypeRepository.deleteById(id);
    }

    @Transactional
    public TicketTypeResponse toggleDynamicPricing(Long id, boolean active, User user) {
        Festival festival = requireTicketTypeAccess(user);
        TicketType tt = ticketTypeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket type does not belong to your festival");
        }
        tt.setDynamicPricingActive(active);
        tt = ticketTypeRepository.save(tt);
        return buildResponse(tt);
    }

    private void saveSegments(TicketType tt, List<Long> segmentIds, Long festivalId) {
        if (segmentIds == null || segmentIds.isEmpty()) return;
        for (Long segmentId : segmentIds) {
            Segment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found: " + segmentId));
            if (!segment.getFestival().getFestivalId().equals(festivalId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment does not belong to this festival");
            }
            ticketTypeSegmentRepository.save(
                TicketTypeSegment.builder()
                    .ticketType(tt)
                    .segment(segment)
                    .build()
            );
        }
    }
}
