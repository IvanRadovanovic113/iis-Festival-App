package com.festivalapp.prodaja.service;

import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.CheckoutPreviewResponse;
import com.festivalapp.prodaja.dto.PurchaseRequest;
import com.festivalapp.prodaja.dto.PurchaseResponse;
import com.festivalapp.prodaja.model.*;
import com.festivalapp.prodaja.repository.*;
import com.festivalapp.service.TicketMailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final PriceLockRepository priceLockRepository;
    private final TierConfigService tierConfigService;
    private final TicketMailService ticketMailService;

    // ────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ────────────────────────────────────────────────────────────────────────

    @Transactional
    public CheckoutPreviewResponse preview(PurchaseRequest req, User user) {
        Kupac kupac = requireBuyer(user);
        Checkout c = buildCheckout(req, kupac);

        LocalDateTime now = LocalDateTime.now();
        PriceLock lock = priceLockRepository.save(PriceLock.builder()
            .ticketType(c.ticketType())
            .kupac(kupac)
            .promoCode(c.promoCode())
            .bundleDeal(c.bestBundle())
            .lockedQuantity(req.getQuantity())
            .lockedPricePerTicket(c.pricePerTicket())
            .lockedBaseTotal(c.baseTotal())
            .lockedFinalPrice(c.finalPrice())
            .lockedTotalTickets(c.totalTickets())
            .lockedFreeTickets(c.freeTickets())
            .lockedPromoDiscountPct(c.promoDiscountPct())
            .lockedTierDiscountPct(c.tierDiscountPct())
            .lockedTotalDiscountPct(c.totalDiscountPct())
            .lockedBundleApplications(c.bundleApplications())
            .lockedAt(now)
            .expiresAt(now.plusMinutes(10))
            .used(false)
            .build());

        return toPreviewResponse(c, lock);
    }

    @Transactional
    public PurchaseResponse purchase(PurchaseRequest req, User user) {
        Kupac kupac = requireBuyer(user);

        if (req.getPriceLockId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price lock is required — please load checkout preview first");
        }
        PriceLock lock = priceLockRepository.findById(req.getPriceLockId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price lock not found"));

        if (lock.isUsed())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Price lock already used");
        if (lock.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.GONE, "Price lock expired — please preview again");
        if (!lock.getKupac().getKupacId().equals(kupac.getKupacId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Price lock belongs to another user");
        if (!lock.getTicketType().getTicketTypeId().equals(req.getTicketTypeId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price lock ticket type mismatch");

        // Jedina re-validacija: dostupnost karata
        TicketType ticketType = lock.getTicketType();
        int available = ticketType.getTotalQuantity() - ticketType.getSoldCount();
        if (lock.getLockedQuantity() > available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Not enough tickets available. Requested: " + lock.getLockedQuantity() + ", available: " + available);
        }

        lock.setUsed(true);
        priceLockRepository.save(lock);

        // Snimi kupovinu — sve vrednosti dolaze iz locka
        Kupovina kupovina = Kupovina.builder()
            .kupac(kupac)
            .ticketType(ticketType)
            .promoCode(lock.getPromoCode())
            .bundleDeal(lock.getBundleDeal())
            .datum(LocalDateTime.now())
            .kolicina(lock.getLockedTotalTickets())
            .ukupnaCena(lock.getLockedFinalPrice())
            .build();
        kupovina = kupovinaRepository.save(kupovina);

        // Ažuriraj usedCount promo koda
        if (lock.getPromoCode() != null) {
            lock.getPromoCode().setUsedCount(lock.getPromoCode().getUsedCount() + 1);
            promoCodeRepository.save(lock.getPromoCode());
        }

        // Ažuriraj usedCount bundle deal-a
        if (lock.getBundleDeal() != null && lock.getLockedBundleApplications() > 0) {
            lock.getBundleDeal().setUsedCount(lock.getBundleDeal().getUsedCount() + lock.getLockedBundleApplications());
            bundleDealRepository.save(lock.getBundleDeal());
        }

        // Kreiraj karte (DB trigger ažurira soldCount i ukupnoKupovina)
        List<Karta> karte = new ArrayList<>();
        for (int i = 0; i < lock.getLockedTotalTickets(); i++) {
            karte.add(kartaRepository.save(Karta.builder()
                .kupovina(kupovina)
                .qrKod(generateQrKod())
                .build()));
        }

        // Flush pa refresh — trigger je ažurirao ukupnoKupovina direktno u DB,
        // JPA cache još ima staru vrednost pa moramo eksplicitno da učitamo iz DB
        entityManager.flush();
        entityManager.refresh(kupac);
        tierConfigService.evaluateAndUpgrade(kupac);

        ticketMailService.sendTicketEmail(user, kupovina, karte);
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

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredLocks() {
        priceLockRepository.deleteExpiredLocks(LocalDateTime.now());
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

        // 3. Aktivna cena — currentPrice ako postoji (dynamic pricing), inače basePrice
        BigDecimal pricePerTicket = pricingPeriodRepository
            .findActiveForTicketType(ticketType.getTicketTypeId(), today)
            .map(p -> p.getCurrentPrice() != null ? p.getCurrentPrice() : p.getBasePrice())
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

    private CheckoutPreviewResponse toPreviewResponse(Checkout c, PriceLock lock) {
        String bundleDesc = c.bestBundle() != null
            ? "Kupi " + c.bestBundle().getKupiKarata() + ", dobij " + c.bestBundle().getDobijaKarata() + " gratis"
            : null;

        return CheckoutPreviewResponse.builder()
            .ticketTypeId(c.ticketType().getTicketTypeId())
            .ticketTypeName(c.ticketType().getName())
            .pricePerTicket(c.pricePerTicket())
            .quantityPaid(c.totalTickets() - c.freeTickets())
            .baseTotal(c.baseTotal())
            .promoCodeApplied(c.promoCode() != null ? c.promoCode().getCode() : null)
            .promoDiscountPercent(c.promoDiscountPct())
            .tierName(c.tierLevel() == KupacTier.STANDARD ? null : c.tierLevel().name())
            .tierDiscountPercent(c.tierDiscountPct())
            .totalDiscountPercent(c.totalDiscountPct())
            .freeTickets(c.freeTickets())
            .bundleDealDescription(bundleDesc)
            .totalTickets(c.totalTickets())
            .finalPrice(c.finalPrice())
            .availableCount(c.availableCount())
            .priceLockId(lock.getId())
            .priceLockExpiresAt(lock.getExpiresAt())
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
