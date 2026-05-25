package com.festivalapp.prodaja.service;

import com.festivalapp.prodaja.dto.TierConfigRequest;
import com.festivalapp.prodaja.dto.TierConfigResponse;
import com.festivalapp.prodaja.model.Kupac;
import com.festivalapp.prodaja.model.KupacTier;
import com.festivalapp.prodaja.model.TierConfig;
import com.festivalapp.prodaja.repository.KupacRepository;
import com.festivalapp.prodaja.repository.TierConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TierConfigService {

    private final TierConfigRepository tierConfigRepository;
    private final KupacRepository kupacRepository;

    /** Redosled tier-a od najnižeg ka najvišem */
    private static final List<KupacTier> TIER_RANK =
        List.of(KupacTier.STANDARD, KupacTier.BRONZE, KupacTier.SILVER, KupacTier.GOLD);

    // ─── Public API ──────────────────────────────────────────────────────────

    /** Vraća sve tier konfiguracije sortirane od BRONZE → SILVER → GOLD */
    public List<TierConfigResponse> getAll() {
        return tierConfigRepository.findAll().stream()
            .sorted(Comparator.comparingInt(c -> rankOf(c.getTier())))
            .map(TierConfigResponse::from)
            .toList();
    }

    /** Ažurira threshold i popust za dati tier, pa odmah retroaktivno upgradeuje sve kupce */
    @Transactional
    public TierConfigResponse update(KupacTier tier, TierConfigRequest req) {
        TierConfig config = tierConfigRepository.findById(tier)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Tier config not found: " + tier));
        config.setMinTickets(req.getMinTickets());
        config.setDiscountPercent(req.getDiscountPercent());
        TierConfigResponse saved = TierConfigResponse.from(tierConfigRepository.save(config));
        reevaluateAllKupci();
        return saved;
    }

    /** Vraća popust za dati tier (0 za STANDARD) */
    public int getDiscountForTier(KupacTier tier) {
        if (tier == KupacTier.STANDARD) return 0;
        return tierConfigRepository.findById(tier)
            .map(TierConfig::getDiscountPercent)
            .orElse(0);
    }

    /**
     * Proverava da li kupac zaslužuje viši tier i upgradeuje ga.
     * Nikad ne downgradeuje — kupac može samo napredovati.
     */
    public void evaluateAndUpgrade(Kupac kupac) {
        List<TierConfig> configs = sortedDescByMinTickets();
        KupacTier earned = earnedTier(kupac.getUkupnoKupovina(), configs);
        if (rankOf(earned) > rankOf(kupac.getTier())) {
            kupac.setTier(earned);
            kupacRepository.save(kupac);
        }
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /** Prolazi kroz sve kupce i upgradeuje sve koji su to zaslužili */
    private void reevaluateAllKupci() {
        List<TierConfig> configs = sortedDescByMinTickets();
        for (Kupac kupac : kupacRepository.findAll()) {
            KupacTier earned = earnedTier(kupac.getUkupnoKupovina(), configs);
            if (rankOf(earned) > rankOf(kupac.getTier())) {
                kupac.setTier(earned);
                kupacRepository.save(kupac);
            }
        }
    }

    /**
     * Vraća najviši tier koji kupac zaslužuje na osnovu broja kupljenih karata.
     * Configs mora biti sortiran opadajuće po minTickets.
     */
    private KupacTier earnedTier(int ukupnoKupovina, List<TierConfig> configs) {
        for (TierConfig config : configs) {
            if (ukupnoKupovina >= config.getMinTickets()) {
                return config.getTier();
            }
        }
        return KupacTier.STANDARD;
    }

    private List<TierConfig> sortedDescByMinTickets() {
        return tierConfigRepository.findAll().stream()
            .sorted(Comparator.comparingInt(TierConfig::getMinTickets).reversed())
            .toList();
    }

    private int rankOf(KupacTier tier) {
        return TIER_RANK.indexOf(tier);
    }
}
