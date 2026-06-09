package com.festivalapp.prodaja.service;

import com.festivalapp.prodaja.model.*;
import com.festivalapp.prodaja.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicPricingService {

    private final TicketTypeRepository ticketTypeRepository;
    private final PricingPeriodRepository pricingPeriodRepository;
    private final OcekivanaProdajaRepository ocekivanaProdajaRepository;
    private final PregledProdajeRepository pregledProdajeRepository;
    private final CenovnaIstorijaRepository cenovnaIstorijaRepository;
    private final KupovinaRepository kupovinaRepository;

    @Scheduled(fixedRate = 60_000)
    public void runCheck() {
        List<TicketType> aktivni = ticketTypeRepository.findByDynamicPricingActiveTrue();
        for (TicketType tt : aktivni) {
            try {
                processTicketType(tt);
            } catch (Exception e) {
                log.error("Dynamic pricing error for ticketType {}: {}", tt.getTicketTypeId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void processTicketType(TicketType tt) {
        Optional<PricingPeriod> periodOpt = pricingPeriodRepository
            .findActiveForTicketType(tt.getTicketTypeId(), LocalDate.now());
        if (periodOpt.isEmpty()) return;

        PricingPeriod period = periodOpt.get();
        if (!Boolean.TRUE.equals(period.getDynamicPricingActive())) return;

        Optional<OcekivanaProdaja> configOpt = ocekivanaProdajaRepository
            .findByPricingPeriod_PricingPeriodId(period.getPricingPeriodId());
        if (configOpt.isEmpty()) return;

        OcekivanaProdaja config = configOpt.get();

        // Zabeležiti vreme provere na samom početku — koristiti za datum PregledProdaje
        // kako bi sledeći interval bio tačan bez drifta od DB operacija
        LocalDateTime checkTime = LocalDateTime.now();

        // Provjeri da li je prošlo intervalMinuti od poslednje provjere
        Optional<PregledProdaje> lastPregled = pregledProdajeRepository
            .findTopByPricingPeriod_PricingPeriodIdOrderByDatumDesc(period.getPricingPeriodId());
        if (lastPregled.isPresent()) {
            LocalDateTime nextCheck = lastPregled.get().getDatum()
                .plusMinutes(config.getIntervalMinuti());
            if (checkTime.isBefore(nextCheck)) return;
        }

        // Stvarna prodaja u vremenskom prozoru
        LocalDateTime from = checkTime.minusHours(config.getBrojSati());
        int actualSales = kupovinaRepository.sumKolicinaSince(tt.getTicketTypeId(), from);
        int expectedSales = config.getBrojKarata();

        // Trend faktor
        double trendFactor = (double)(actualSales - expectedSales) / Math.max(expectedSales, 1);

        // Scarcity faktor
        int remaining = tt.getTotalQuantity() - tt.getSoldCount();
        double remainingPct = tt.getTotalQuantity() > 0
            ? (double) remaining * 100.0 / tt.getTotalQuantity()
            : 0;
        double scarcityBoost = scarcityBoost(remainingPct, config);

        // Korekcija bez momentuma
        double agresivnost = config.getAgresivnost().doubleValue();
        double baseAdj = agresivnost * trendFactor * scarcityBoost;

        // Momentum — težinski prosek poslednjih 5 promena (novije = veća težina)
        // pozitivan = cena je rasla, negativan = cena je padala
        double weightedMomentum = calculateWeightedMomentum(tt.getTicketTypeId(), period.getPricingPeriodId());
        double momentumStrength = Math.abs(weightedMomentum) * 10; // skaliranje na razuman opseg
        boolean isSameDirection = (weightedMomentum >= 0 && baseAdj >= 0)
                               || (weightedMomentum <= 0 && baseAdj <= 0);

        double momentumMultiplier;
        if (isSameDirection) {
            // Momentum pojačava korekciju u istom smeru
            momentumMultiplier = 1.0 + momentumStrength;
        } else {
            // Momentum ublaži korekciju u suprotnom smeru (sistem je do sada išao dobro)
            momentumMultiplier = Math.max(0.2, 1.0 - momentumStrength);
        }

        double rawAdj = baseAdj * momentumMultiplier;

        if (rawAdj == 0) {
            snimPregledProdaje(period, actualSales, expectedSales, checkTime);
            return;
        }

        BigDecimal staraCena = period.getCurrentPrice() != null
            ? period.getCurrentPrice() : period.getBasePrice();
        BigDecimal novaCena = staraCena
            .multiply(BigDecimal.valueOf(1.0 + rawAdj))
            .setScale(2, RoundingMode.HALF_UP);

        // Donja granica = minPrice
        if (novaCena.compareTo(period.getMinPrice()) < 0) {
            novaCena = period.getMinPrice();
        }

        // Nema promene ako je ista cena
        if (novaCena.compareTo(staraCena) == 0) {
            snimPregledProdaje(period, actualSales, expectedSales, checkTime);
            return;
        }

        period.setCurrentPrice(novaCena);
        pricingPeriodRepository.save(period);

        String razlog = String.format(
            "trend=%.2f, scarcity=%.0f%% (boost=%.2f), momentum=%d, adj=%.3f",
            trendFactor, remainingPct, scarcityBoost, momentum, rawAdj
        );

        CenovnaIstorija zapis = CenovnaIstorija.builder()
            .ticketType(tt)
            .pricingPeriod(period)
            .datum(checkTime)
            .staraCena(staraCena)
            .novaCena(novaCena)
            .razlog(razlog)
            .jeRucnaPromena(false)
            .build();
        cenovnaIstorijaRepository.save(zapis);

        snimPregledProdaje(period, actualSales, expectedSales, checkTime);

        log.info("Dynamic pricing: ticketType={}, {} → {} ({})",
            tt.getTicketTypeId(), staraCena, novaCena, razlog);
    }

    private void snimPregledProdaje(PricingPeriod period, int actual, int expected, LocalDateTime checkTime) {
        PregledProdaje pregled = PregledProdaje.builder()
            .pricingPeriod(period)
            .datum(checkTime)
            .prodajeUPerioduU(actual)
            .ocekivanoPeriodU(expected)
            .build();
        pregledProdajeRepository.save(pregled);
    }

    private double scarcityBoost(double remainingPct, OcekivanaProdaja config) {
        if (remainingPct <= config.getScarcityPragVisok()) {
            return config.getScarcityMultiplikatorVisok().doubleValue();
        }
        if (remainingPct <= config.getScarcityPragNizak()) {
            return config.getScarcityMultiplikatorNizak().doubleValue();
        }
        return 1.0;
    }

    /**
     * Izračunava težinski momentum na osnovu poslednjih 5 automatskih promena.
     * Novije promene imaju veću težinu (decay faktor 0.6 po koraku).
     *
     * @return pozitivan = cena je generalno rasla, negativan = padala, 0 = nema istorije
     */
    private double calculateWeightedMomentum(Long ticketTypeId, Long periodId) {
        List<CenovnaIstorija> history = cenovnaIstorijaRepository
            .findTop5ByTicketType_TicketTypeIdAndPricingPeriod_PricingPeriodIdAndJeRucnaPromenaFalseOrderByDatumDesc(
                ticketTypeId, periodId);

        if (history.isEmpty()) return 0.0;

        double weightedSum = 0.0;
        double totalWeight = 0.0;
        double decay = 1.0;

        for (CenovnaIstorija entry : history) {
            if (entry.getStaraCena().compareTo(BigDecimal.ZERO) == 0) {
                decay *= 0.6;
                continue;
            }
            // Relativna promena: (novaCena - staraCena) / staraCena
            double relChange = entry.getNovaCena()
                .subtract(entry.getStaraCena())
                .divide(entry.getStaraCena(), 6, RoundingMode.HALF_UP)
                .doubleValue();

            weightedSum += relChange * decay;
            totalWeight += decay;
            decay *= 0.6; // svaka starija promena ima 40% manju težinu
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }
}
