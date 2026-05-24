package com.festivalapp.service;

import com.festivalapp.dto.PromoCodeRequest;
import com.festivalapp.dto.PromoCodeResponse;
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
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireAccess(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_MANAGER && assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return assignment.getFestival();
    }

    private PromoCode getOwnedPromoCode(Long id, Festival festival) {
        PromoCode promo = promoCodeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found"));
        if (!promo.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Promo code does not belong to your festival");
        }
        return promo;
    }

    public List<PromoCodeResponse> getAll(User user) {
        Festival festival = requireAccess(user);
        return promoCodeRepository
            .findByFestival_FestivalIdOrderByValidFromDesc(festival.getFestivalId())
            .stream().map(PromoCodeResponse::from).toList();
    }

    @Transactional
    public PromoCodeResponse create(PromoCodeRequest request, User user) {
        Festival festival = requireAccess(user);
        validate(request, festival.getFestivalId(), null);

        PromoCode promo = PromoCode.builder()
            .code(request.getCode().trim().toUpperCase())
            .discountPercent(request.getDiscountPercent())
            .validFrom(request.getValidFrom())
            .validTo(request.getValidTo())
            .maxUses(request.getMaxUses())
            .festival(festival)
            .build();

        return PromoCodeResponse.from(promoCodeRepository.save(promo));
    }

    @Transactional
    public PromoCodeResponse update(Long id, PromoCodeRequest request, User user) {
        Festival festival = requireAccess(user);
        PromoCode promo = getOwnedPromoCode(id, festival);
        validate(request, festival.getFestivalId(), id);

        promo.setCode(request.getCode().trim().toUpperCase());
        promo.setDiscountPercent(request.getDiscountPercent());
        promo.setValidFrom(request.getValidFrom());
        promo.setValidTo(request.getValidTo());
        promo.setMaxUses(request.getMaxUses());

        return PromoCodeResponse.from(promoCodeRepository.save(promo));
    }

    @Transactional
    public void delete(Long id, User user) {
        Festival festival = requireAccess(user);
        PromoCode promo = getOwnedPromoCode(id, festival);
        if (promo.getUsedCount() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot delete a promo code that has been used " + promo.getUsedCount() + " time(s)");
        }
        promoCodeRepository.deleteById(id);
    }

    @Transactional
    public PromoCodeResponse toggleActive(Long id, User user) {
        Festival festival = requireAccess(user);
        PromoCode promo = getOwnedPromoCode(id, festival);
        promo.setActive(!promo.getActive());
        return PromoCodeResponse.from(promoCodeRepository.save(promo));
    }

    private void validate(PromoCodeRequest req, Long festivalId, Long excludeId) {
        if (req.getValidTo().isBefore(req.getValidFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid-to date must be after valid-from date");
        }
        String code = req.getCode().trim().toUpperCase();
        boolean duplicate = excludeId == null
            ? promoCodeRepository.existsByCodeAndFestival_FestivalId(code, festivalId)
            : promoCodeRepository.existsByCodeAndFestival_FestivalIdAndPromoCodeIdNot(code, festivalId, excludeId);
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A promo code with this code already exists");
        }
    }
}
