package com.festivalapp.service;

import com.festivalapp.dto.PricingPeriodRequest;
import com.festivalapp.dto.PricingPeriodResponse;
import com.festivalapp.model.*;
import com.festivalapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingPeriodService {

    private final PricingPeriodRepository pricingPeriodRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private TicketType requireAccess(Long ticketTypeId, User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_MANAGER && assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: sales manager or sales director role required");
        }
        TicketType tt = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(assignment.getFestival().getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket type does not belong to your festival");
        }
        return tt;
    }

    public List<PricingPeriodResponse> getAll(Long ticketTypeId, User user) {
        requireAccess(ticketTypeId, user);
        return pricingPeriodRepository.findByTicketType_TicketTypeIdOrderByStartDateAsc(ticketTypeId)
            .stream().map(PricingPeriodResponse::from).toList();
    }

    @Transactional
    public PricingPeriodResponse create(Long ticketTypeId, PricingPeriodRequest request, User user) {
        TicketType tt = requireAccess(ticketTypeId, user);
        validateRequest(request, ticketTypeId, tt, null);

        PricingPeriod period = PricingPeriod.builder()
            .ticketType(tt)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .basePrice(request.getBasePrice())
            .minPrice(request.getMinPrice())
            .dynamicPricingActive(request.getDynamicPricingActive())
            .build();

        return PricingPeriodResponse.from(pricingPeriodRepository.save(period));
    }

    @Transactional
    public PricingPeriodResponse update(Long ticketTypeId, Long periodId, PricingPeriodRequest request, User user) {
        TicketType tt = requireAccess(ticketTypeId, user);
        PricingPeriod period = pricingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pricing period not found"));
        if (!period.getTicketType().getTicketTypeId().equals(ticketTypeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period does not belong to this ticket type");
        }
        validateRequest(request, ticketTypeId, tt, periodId);

        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setBasePrice(request.getBasePrice());
        period.setMinPrice(request.getMinPrice());
        period.setDynamicPricingActive(request.getDynamicPricingActive());

        return PricingPeriodResponse.from(pricingPeriodRepository.save(period));
    }

    @Transactional
    public void delete(Long ticketTypeId, Long periodId, User user) {
        requireAccess(ticketTypeId, user);
        PricingPeriod period = pricingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pricing period not found"));
        if (!period.getTicketType().getTicketTypeId().equals(ticketTypeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period does not belong to this ticket type");
        }
        pricingPeriodRepository.deleteById(periodId);
    }

    private void validateRequest(PricingPeriodRequest req, Long ticketTypeId, TicketType tt, Long excludeId) {
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (req.getMinPrice().compareTo(req.getBasePrice()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Min price cannot exceed base price");
        }
        if (Boolean.TRUE.equals(req.getDynamicPricingActive()) && !Boolean.TRUE.equals(tt.getDynamicPricingActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Cannot enable dynamic pricing on a period when global dynamic pricing is disabled for this ticket type");
        }
        boolean overlaps = excludeId == null
            ? pricingPeriodRepository.existsOverlapping(ticketTypeId, req.getStartDate(), req.getEndDate())
            : pricingPeriodRepository.existsOverlappingExcluding(ticketTypeId, req.getStartDate(), req.getEndDate(), excludeId);
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This date range overlaps with an existing pricing period");
        }
    }
}
