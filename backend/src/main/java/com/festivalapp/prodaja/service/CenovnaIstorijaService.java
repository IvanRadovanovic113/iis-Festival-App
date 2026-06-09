package com.festivalapp.prodaja.service;

import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.prodaja.dto.CenovnaIstorijaResponse;
import com.festivalapp.prodaja.model.CenovnaIstorija;
import com.festivalapp.prodaja.model.PricingPeriod;
import com.festivalapp.prodaja.model.TicketType;
import com.festivalapp.prodaja.repository.CenovnaIstorijaRepository;
import com.festivalapp.prodaja.repository.TicketTypeRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CenovnaIstorijaService {

    private final CenovnaIstorijaRepository cenovnaIstorijaRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    public Page<CenovnaIstorijaResponse> getHistory(Long ticketTypeId, User user, Pageable pageable) {
        requireAccess(ticketTypeId, user);
        return cenovnaIstorijaRepository
            .findByTicketType_TicketTypeIdOrderByDatumDesc(ticketTypeId, pageable)
            .map(CenovnaIstorijaResponse::from);
    }

    @Transactional
    public void logPromenu(TicketType ticketType, PricingPeriod period,
                           BigDecimal staraCena, BigDecimal novaCena,
                           String razlog, boolean jeRucna) {
        CenovnaIstorija log = CenovnaIstorija.builder()
            .ticketType(ticketType)
            .pricingPeriod(period)
            .datum(LocalDateTime.now())
            .staraCena(staraCena)
            .novaCena(novaCena)
            .razlog(razlog)
            .jeRucnaPromena(jeRucna)
            .build();
        cenovnaIstorijaRepository.save(log);
    }

    private void requireAccess(Long ticketTypeId, User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_MANAGER && assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        TicketType tt = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        if (!tt.getFestival().getFestivalId().equals(assignment.getFestival().getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket type does not belong to your festival");
        }
    }
}
