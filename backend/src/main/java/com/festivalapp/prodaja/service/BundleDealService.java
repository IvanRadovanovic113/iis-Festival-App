package com.festivalapp.prodaja.service;

import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.prodaja.dto.BundleDealRequest;
import com.festivalapp.prodaja.dto.BundleDealResponse;
import com.festivalapp.prodaja.model.BundleDeal;
import com.festivalapp.prodaja.model.TicketType;
import com.festivalapp.prodaja.repository.BundleDealRepository;
import com.festivalapp.prodaja.repository.TicketTypeRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BundleDealService {

    private final BundleDealRepository bundleDealRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireAccess(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_MANAGER && assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return assignment.getFestival();
    }

    private BundleDeal getOwned(Long id, Festival festival) {
        BundleDeal deal = bundleDealRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle deal not found"));
        if (!deal.getTicketType().getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bundle deal does not belong to your festival");
        }
        return deal;
    }

    private TicketType resolveTicketType(Long ticketTypeId, Festival festival) {
        TicketType tt = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket type does not belong to your festival");
        }
        return tt;
    }

    public List<BundleDealResponse> getAll(User user) {
        Festival festival = requireAccess(user);
        return bundleDealRepository
            .findByTicketType_Festival_FestivalIdOrderByVaziOdDesc(festival.getFestivalId())
            .stream().map(BundleDealResponse::from).toList();
    }

    @Transactional
    public BundleDealResponse create(BundleDealRequest request, User user) {
        Festival festival = requireAccess(user);
        validateDates(request);
        TicketType tt = resolveTicketType(request.getTicketTypeId(), festival);

        BundleDeal deal = BundleDeal.builder()
            .ticketType(tt)
            .kupiKarata(request.getKupiKarata())
            .dobijaKarata(request.getDobijaKarata())
            .vaziOd(request.getVaziOd())
            .vaziDo(request.getVaziDo())
            .dostupnoAkcija(request.getDostupnoAkcija())
            .build();

        return BundleDealResponse.from(bundleDealRepository.save(deal));
    }

    @Transactional
    public BundleDealResponse update(Long id, BundleDealRequest request, User user) {
        Festival festival = requireAccess(user);
        BundleDeal deal = getOwned(id, festival);
        validateDates(request);

        if (request.getDostupnoAkcija() < deal.getUsedCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Available count cannot be less than already used count (" + deal.getUsedCount() + ")");
        }

        TicketType tt = resolveTicketType(request.getTicketTypeId(), festival);
        deal.setTicketType(tt);
        deal.setKupiKarata(request.getKupiKarata());
        deal.setDobijaKarata(request.getDobijaKarata());
        deal.setVaziOd(request.getVaziOd());
        deal.setVaziDo(request.getVaziDo());
        deal.setDostupnoAkcija(request.getDostupnoAkcija());

        return BundleDealResponse.from(bundleDealRepository.save(deal));
    }

    @Transactional
    public void delete(Long id, User user) {
        Festival festival = requireAccess(user);
        BundleDeal deal = getOwned(id, festival);
        if (deal.getUsedCount() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot delete a bundle deal that has been used " + deal.getUsedCount() + " time(s)");
        }
        bundleDealRepository.deleteById(id);
    }

    @Transactional
    public BundleDealResponse toggleActive(Long id, User user) {
        Festival festival = requireAccess(user);
        BundleDeal deal = getOwned(id, festival);
        deal.setActive(!deal.getActive());
        return BundleDealResponse.from(bundleDealRepository.save(deal));
    }

    private void validateDates(BundleDealRequest req) {
        if (req.getVaziDo().isBefore(req.getVaziOd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
    }
}
