package com.festivalapp.prodaja.service;

import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.CheckoutPreviewResponse;
import com.festivalapp.prodaja.dto.PurchaseRequest;
import com.festivalapp.prodaja.dto.PurchaseResponse;
import com.festivalapp.prodaja.model.*;
import com.festivalapp.prodaja.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    @PersistenceContext
    private EntityManager entityManager;

    private final KupacRepository kupacRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PricingPeriodRepository pricingPeriodRepository;
    private final BundleDealRepository bundleDealRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final KupovinaRepository kupovinaRepository;
    private final KartaRepository kartaRepository;
    private final TierConfigService tierConfigService;

    // ────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ────────────────────────────────────────────────────────────────────────

    public CheckoutPreviewResponse preview(PurchaseRequest req, User user) {
        Kupac kupac = requireBuyer(user);
        Checkout c = buildCheckout(req, kupac);
        return toPreviewResponse(c);
    }

    @Transactional
    public PurchaseResponse purchase(PurchaseRequest req, User user) {
        Kupac kupac = requireBuyer(user);
        Checkout c = buildCheckout(req, kupac);

        // Snimi kupovinu
        Kupovina kupovina = Kupovina.builder()
            .kupac(kupac)
            .ticketType(c.ticketType)
            .promoCode(c.promoCode)
            .bundleDeal(c.bestBundle)
            .datum(LocalDateTime.now())
            .kolicina(c.totalTickets)
            .ukupnaCena(c.finalPrice)
            .build();
        kupovina = kupovinaRepository.save(kupovina);

        // Ažuriraj usedCount promo koda
        if (c.promoCode != null) {
            c.promoCode.setUsedCount(c.promoCode.getUsedCount() + 1);
            promoCodeRepository.save(c.promoCode);
        }

        // Ažuriraj usedCount bundle deal-a (broj primena)
        if (c.bestBundle != null && c.bundleApplications > 0) {
            c.bestBundle.setUsedCount(c.bestBundle.getUsedCount() + c.bundleApplications);
            bundleDealRepository.save(c.bestBundle);
        }

        // Kreiraj karte (DB trigger ažurira soldCount i ukupnoKupovina)
        List<Karta> karte = new ArrayList<>();
        for (int i = 0; i < c.totalTickets; i++) {
            Karta karta = Karta.builder()
                .kupovina(kupovina)
                .qrKod(generateQrKod())
                .build();
            karte.add(kartaRepository.save(karta));
        }

        // Flush pa refresh — trigger je ažurirao ukupnoKupovina direktno u DB,
        // JPA cache još ima staru vrednost pa moramo eksplicitno da učitamo iz DB
        entityManager.flush();
        entityManager.refresh(kupac);
        tierConfigService.evaluateAndUpgrade(kupac);

        return PurchaseResponse.from(kupovina, karte);
    }

    public List<PurchaseResponse> getMyPurchases(User user) {
        Kupac kupac = requireBuyer(user);
        return kupovinaRepository
            .findByKupac_KupacIdOrderByDatumDesc(kupac.getKupacId())
            .stream()
            .map(k -> PurchaseResponse.from(k, kartaRepository.findByKupovina_KupovinaId(k.getKupovinaId())))
            .toList();
    }

    // ────────────────────────────────────────────────────────────────────────
    // CHECKOUT CALCULATION
    // ────────────────────────────────────────────────────────────────────────

    private Checkout buildCheckout(PurchaseRequest req, Kupac kupac) {
        LocalDate today = LocalDate.now();
        int quantity = req.getQuantity();

        // 1. Tip karte
        TicketType ticketType = ticketTypeRepository.findById(req.getTicketTypeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));

        // 2. Dostupnost
        int available = ticketType.getTotalQuantity() - ticketType.getSoldCount();
        if (quantity > available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Not enough tickets available. Requested: " + quantity + ", available: " + available);
        }

        // 3. Aktivna cena
        BigDecimal pricePerTicket = pricingPeriodRepository
            .findActiveForTicketType(ticketType.getTicketTypeId(), today)
            .map(p -> p.getBasePrice())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No active pricing period for this ticket type"));

        BigDecimal baseTotal = pricePerTicket.multiply(BigDecimal.valueOf(quantity));

        // 4. Promo kod
        PromoCode promoCode = null;
        int promoDiscountPct = 0;
        if (req.getPromoCode() != null && !req.getPromoCode().isBlank()) {
            String code = req.getPromoCode().trim().toUpperCase();
            promoCode = promoCodeRepository
                .findByCodeAndFestival_FestivalId(code, ticketType.getFestival().getFestivalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Promo code '" + code + "' is not valid for this festival"));

            if (!promoCode.getActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code is inactive");
            }
            if (today.isBefore(promoCode.getValidFrom()) || today.isAfter(promoCode.getValidTo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code has expired or is not yet valid");
            }
            if (promoCode.getMaxUses() != null && promoCode.getUsedCount() >= promoCode.getMaxUses()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code has reached its usage limit");
            }
            promoDiscountPct = promoCode.getDiscountPercent();
        }

        // 5. Tier popust
        int tierDiscountPct = tierConfigService.getDiscountForTier(kupac.getTier());

        // 6. Ukupni popust (sabiranje, max 100)
        int totalDiscountPct = Math.min(100, promoDiscountPct + tierDiscountPct);

        // 7. Finalna cena
        BigDecimal finalPrice = baseTotal
            .multiply(BigDecimal.valueOf(100 - totalDiscountPct))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 8. Najbolji bundle deal (max gratis karata)
        int slotsForFree = available - quantity;
        List<BundleDeal> activeBundles = bundleDealRepository
            .findActiveByTicketType(ticketType.getTicketTypeId(), today);

        BundleDeal bestBundle = null;
        int maxFreeTickets = 0;
        int bestBundleApplications = 0;

        for (BundleDeal b : activeBundles) {
            int byQty      = quantity / b.getKupiKarata();
            int byCapacity = b.getDostupnoAkcija() - b.getUsedCount();
            int byAvail    = (b.getDobijaKarata() > 0) ? slotsForFree / b.getDobijaKarata() : 0;
            int apps       = Math.min(byQty, Math.min(byCapacity, byAvail));
            int free       = apps * b.getDobijaKarata();
            if (free > maxFreeTickets) {
                maxFreeTickets        = free;
                bestBundle            = b;
                bestBundleApplications = apps;
            }
        }

        int totalTickets = quantity + maxFreeTickets;

        return new Checkout(
            ticketType, pricePerTicket, baseTotal,
            promoCode, promoDiscountPct,
            tierDiscountPct, totalDiscountPct,
            finalPrice,
            bestBundle, bestBundleApplications, maxFreeTickets,
            totalTickets, available, kupac.getTier()
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ────────────────────────────────────────────────────────────────────────

    private Kupac requireBuyer(User user) {
        if (user.getRole() != Role.BUYER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only buyers can purchase tickets");
        }
        return kupacRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer profile not found"));
    }

    private String generateQrKod() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private CheckoutPreviewResponse toPreviewResponse(Checkout c) {
        String bundleDesc = c.bestBundle != null
            ? "Kupi " + c.bestBundle.getKupiKarata() + ", dobij " + c.bestBundle.getDobijaKarata() + " gratis"
            : null;

        return CheckoutPreviewResponse.builder()
            .ticketTypeId(c.ticketType.getTicketTypeId())
            .ticketTypeName(c.ticketType.getName())
            .pricePerTicket(c.pricePerTicket)
            .quantityPaid(c.totalTickets - c.freeTickets)
            .baseTotal(c.baseTotal)
            .promoCodeApplied(c.promoCode != null ? c.promoCode.getCode() : null)
            .promoDiscountPercent(c.promoDiscountPct)
            .tierName(c.tierLevel == KupacTier.STANDARD ? null : c.tierLevel.name())
            .tierDiscountPercent(c.tierDiscountPct)
            .totalDiscountPercent(c.totalDiscountPct)
            .freeTickets(c.freeTickets)
            .bundleDealDescription(bundleDesc)
            .totalTickets(c.totalTickets)
            .finalPrice(c.finalPrice)
            .availableCount(c.availableCount)
            .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // INTERNAL RECORD
    // ────────────────────────────────────────────────────────────────────────

    private record Checkout(
        TicketType ticketType,
        BigDecimal pricePerTicket,
        BigDecimal baseTotal,
        PromoCode promoCode,
        int promoDiscountPct,
        int tierDiscountPct,
        int totalDiscountPct,
        BigDecimal finalPrice,
        BundleDeal bestBundle,
        int bundleApplications,
        int freeTickets,
        int totalTickets,
        int availableCount,
        KupacTier tierLevel
    ) {}
}
