package com.festivalapp.prodaja.service;

import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.prodaja.dto.OcekivanaProdajaRequest;
import com.festivalapp.prodaja.dto.OcekivanaProdajaResponse;
import com.festivalapp.prodaja.model.OcekivanaProdaja;
import com.festivalapp.prodaja.model.PricingPeriod;
import com.festivalapp.prodaja.repository.OcekivanaProdajaRepository;
import com.festivalapp.prodaja.repository.PricingPeriodRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OcekivanaProdajaService {

    private final OcekivanaProdajaRepository ocekivanaProdajaRepository;
    private final PricingPeriodRepository pricingPeriodRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private PricingPeriod requireAccess(Long periodId, User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_MANAGER && assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        PricingPeriod period = pricingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pricing period not found"));
        if (!period.getTicketType().getFestival().getFestivalId().equals(assignment.getFestival().getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pricing period does not belong to your festival");
        }
        if (!Boolean.TRUE.equals(period.getDynamicPricingActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Dynamic pricing is not enabled on this pricing period");
        }
        return period;
    }

    public OcekivanaProdajaResponse get(Long periodId, User user) {
        requireAccess(periodId, user);
        return ocekivanaProdajaRepository.findByPricingPeriod_PricingPeriodId(periodId)
            .map(OcekivanaProdajaResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expected sales not configured for this period"));
    }

    @Transactional
    public OcekivanaProdajaResponse upsert(Long periodId, OcekivanaProdajaRequest request, User user) {
        PricingPeriod period = requireAccess(periodId, user);
        validateRequest(request);

        OcekivanaProdaja entity = ocekivanaProdajaRepository
            .findByPricingPeriod_PricingPeriodId(periodId)
            .orElse(OcekivanaProdaja.builder().pricingPeriod(period).build());

        entity.setBrojKarata(request.getBrojKarata());
        entity.setBrojSati(request.getBrojSati());
        entity.setAgresivnost(request.getAgresivnost());
        entity.setIntervalMinuti(request.getIntervalMinuti());
        entity.setScarcityPragNizak(request.getScarcityPragNizak());
        entity.setScarcityPragVisok(request.getScarcityPragVisok());
        entity.setScarcityMultiplikatorNizak(request.getScarcityMultiplikatorNizak());
        entity.setScarcityMultiplikatorVisok(request.getScarcityMultiplikatorVisok());

        return OcekivanaProdajaResponse.from(ocekivanaProdajaRepository.save(entity));
    }

    private void validateRequest(OcekivanaProdajaRequest req) {
        if (req.getScarcityPragVisok() >= req.getScarcityPragNizak()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "High scarcity threshold must be lower than low scarcity threshold");
        }
        if (req.getScarcityMultiplikatorVisok().compareTo(req.getScarcityMultiplikatorNizak()) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "High scarcity multiplier must be greater than low scarcity multiplier");
        }
    }
}
