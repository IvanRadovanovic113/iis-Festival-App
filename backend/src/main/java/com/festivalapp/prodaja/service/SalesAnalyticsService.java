package com.festivalapp.prodaja.service;

import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.prodaja.dto.*;
import com.festivalapp.prodaja.model.BundleDeal;
import com.festivalapp.prodaja.model.PregledProdaje;
import com.festivalapp.prodaja.model.PricingPeriod;
import com.festivalapp.prodaja.model.PromoCode;
import com.festivalapp.prodaja.model.TicketType;
import com.festivalapp.prodaja.repository.*;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesAnalyticsService {

    private final UserFestivalAssignmentRepository assignmentRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final KupovinaRepository kupovinaRepository;
    private final PricingPeriodRepository pricingPeriodRepository;
    private final PregledProdajeRepository pregledProdajeRepository;
    private final CenovnaIstorijaRepository cenovnaIstorijaRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final BundleDealRepository bundleDealRepository;

    public SalesOverviewResponse getOverview(Long festivalId, User user) {
        Festival festival = requireSalesDirectorFestival(user, festivalId);

        List<TicketType> ticketTypes = ticketTypeRepository.findByFestival_FestivalId(festivalId);

        // Ukupan prihod iz baze
        BigDecimal totalRevenue = kupovinaRepository.sumRevenueForFestival(festivalId);

        // Prihod po tipu karte iz jednog aggregate query-ja
        List<Object[]> revenueRows = kupovinaRepository.revenueByTicketTypeForFestival(festivalId);
        Map<Long, BigDecimal> revenueByType = revenueRows.stream().collect(
            Collectors.toMap(
                r -> ((Number) r[0]).longValue(),
                r -> r[1] != null ? (BigDecimal) r[1] : BigDecimal.ZERO
            ));

        LocalDate today = LocalDate.now();

        List<String> warningMessages = new ArrayList<>();
        List<String> highlightMessages = new ArrayList<>();

        List<TicketTypeSummaryDto> summaries = ticketTypes.stream().map(tt -> {
            // Trenutna cena iz aktivnog pricing perioda
            BigDecimal currentPrice = pricingPeriodRepository
                .findActiveForTicketType(tt.getTicketTypeId(), today)
                .map(p -> p.getCurrentPrice() != null ? p.getCurrentPrice() : p.getBasePrice())
                .orElse(null);

            BigDecimal revenue = revenueByType.getOrDefault(tt.getTicketTypeId(), BigDecimal.ZERO);

            // Performance status iz PregledProdaje
            String status = resolvePerformanceStatus(tt, today, warningMessages, highlightMessages);

            return TicketTypeSummaryDto.builder()
                .ticketTypeId(tt.getTicketTypeId())
                .name(tt.getName())
                .soldCount(tt.getSoldCount())
                .totalQuantity(tt.getTotalQuantity())
                .currentPrice(currentPrice)
                .revenue(revenue)
                .performanceStatus(status)
                .build();
        }).toList();

        int totalSold = ticketTypes.stream().mapToInt(TicketType::getSoldCount).sum();
        int totalAvailable = ticketTypes.stream().mapToInt(TicketType::getTotalQuantity).sum();

        return SalesOverviewResponse.builder()
            .festivalName(festival.getName())
            .totalRevenue(totalRevenue)
            .totalTicketsSold(totalSold)
            .totalTicketsAvailable(totalAvailable)
            .activeTicketTypes(ticketTypes.size())
            .ticketTypes(summaries)
            .salesWarning(!warningMessages.isEmpty())
            .warningMessages(warningMessages)
            .salesHighlight(!highlightMessages.isEmpty())
            .highlightMessages(highlightMessages)
            .build();
    }

    public List<RevenueDataPointDto> getRevenueOverTime(Long festivalId, User user) {
        requireSalesDirectorFestival(user, festivalId);

        return kupovinaRepository.revenueByDayForFestival(festivalId).stream()
            .map(r -> RevenueDataPointDto.builder()
                .date((String) r[0])
                .revenue(r[1] != null ? (BigDecimal) r[1] : BigDecimal.ZERO)
                .ticketsSold(r[2] != null ? ((Number) r[2]).intValue() : 0)
                .build())
            .toList();
    }

    public List<SalesByTicketTypeDto> getSalesByTicketType(Long festivalId, User user) {
        requireSalesDirectorFestival(user, festivalId);

        List<TicketType> ticketTypes = ticketTypeRepository.findByFestival_FestivalId(festivalId);

        List<Object[]> revenueRows = kupovinaRepository.revenueByTicketTypeForFestival(festivalId);
        Map<Long, BigDecimal> revenueByType = revenueRows.stream().collect(
            Collectors.toMap(
                r -> ((Number) r[0]).longValue(),
                r -> r[1] != null ? (BigDecimal) r[1] : BigDecimal.ZERO
            ));

        return ticketTypes.stream().map(tt -> SalesByTicketTypeDto.builder()
            .ticketTypeId(tt.getTicketTypeId())
            .name(tt.getName())
            .soldCount(tt.getSoldCount())
            .revenue(revenueByType.getOrDefault(tt.getTicketTypeId(), BigDecimal.ZERO))
            .totalQuantity(tt.getTotalQuantity())
            .build()).toList();
    }


    public List<TicketTypeSalesDataPointDto> getSalesOverTime(Long ticketTypeId, String from, String to, User user) {
        requireTicketTypeForSalesDirector(ticketTypeId, user);

        String fromTs = (from != null ? from : "1970-01-01") + " 00:00:00";
        String toTs   = (to   != null ? to   : "2999-12-31") + " 23:59:59";

        return kupovinaRepository.salesOverTimeForTicketType(ticketTypeId, fromTs, toTs).stream()
            .map(r -> TicketTypeSalesDataPointDto.builder()
                .date((String) r[0])
                .ticketsSold(r[1] != null ? ((Number) r[1]).intValue() : 0)
                .revenue(r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO)
                .build())
            .toList();
    }

    public List<TicketTypePriceHistoryDto> getTicketTypePriceHistory(Long ticketTypeId, User user) {
        requireTicketTypeForSalesDirector(ticketTypeId, user);

        return cenovnaIstorijaRepository.findByTicketType_TicketTypeIdOrderByDatumAsc(ticketTypeId).stream()
            .map(c -> TicketTypePriceHistoryDto.builder()
                .datum(c.getDatum().toString())
                .oldPrice(c.getStaraCena())
                .newPrice(c.getNovaCena())
                .manual(Boolean.TRUE.equals(c.getJeRucnaPromena()))
                .reason(c.getRazlog())
                .build())
            .toList();
    }

    public List<TicketTypeSalesByPeriodDto> getSalesByPeriod(Long ticketTypeId, User user) {
        requireTicketTypeForSalesDirector(ticketTypeId, user);

        List<PricingPeriod> periods = pricingPeriodRepository
            .findByTicketType_TicketTypeIdOrderByStartDateAsc(ticketTypeId);

        return periods.stream().map(p -> {
            String fromTs = p.getStartDate().toString() + " 00:00:00";
            String toTs   = p.getEndDate().toString()   + " 23:59:59";

            List<Object[]> result = kupovinaRepository.salesInPeriod(ticketTypeId, fromTs, toTs);
            Object[] row = result.isEmpty() ? new Object[]{0, BigDecimal.ZERO} : result.get(0);
            int tickets     = row[0] != null ? ((Number) row[0]).intValue() : 0;
            BigDecimal revenue = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;

            return TicketTypeSalesByPeriodDto.builder()
                .periodId(p.getPricingPeriodId())
                .startDate(p.getStartDate().toString())
                .endDate(p.getEndDate().toString())
                .basePrice(p.getBasePrice())
                .currentPrice(p.getCurrentPrice())
                .ticketsSold(tickets)
                .revenue(revenue)
                .build();
        }).toList();
    }

    private String resolvePerformanceStatus(TicketType tt, LocalDate today,
                                            List<String> warnings, List<String> highlights) {
        Optional<PregledProdaje> latestPregled = pricingPeriodRepository
            .findActiveForTicketType(tt.getTicketTypeId(), today)
            .flatMap(p -> pregledProdajeRepository
                .findTopByPricingPeriod_PricingPeriodIdOrderByDatumDesc(p.getPricingPeriodId()));

        if (latestPregled.isEmpty() || latestPregled.get().getOcekivanoPeriodU() == 0) {
            return "NORMAL";
        }

        PregledProdaje pregled = latestPregled.get();
        double ratio = (double) pregled.getProdajeUPerioduU() / pregled.getOcekivanoPeriodU();

        if (ratio < 0.5) {
            warnings.add("'" + tt.getName() + "' zaostaje za prodajnim planom (" +
                Math.round(ratio * 100) + "% od očekivanog)");
            return "WARNING";
        }
        if (ratio > 1.5) {
            highlights.add("'" + tt.getName() + "' premašuje prodajni plan (" +
                Math.round(ratio * 100) + "% od očekivanog)");
            return "HIGHLIGHT";
        }
        return "NORMAL";
    }

    public List<PromoCodeStatsDto> getPromoCodeStats(Long festivalId, User user) {
        requireSalesDirectorFestival(user, festivalId);

        List<PromoCode> sviKodovi = promoCodeRepository.findByFestival_FestivalIdOrderByValidFromDesc(festivalId);

        // Prihod po promo kodu — sabira ukupnaCena svih kupovina u kojima je taj kod iskoriscen
        Map<Long, BigDecimal> prihodPoKodu = new java.util.HashMap<>();
        for (Object[] row : kupovinaRepository.revenueByPromoCodeForFestival(festivalId)) {
            Long promoCodeId = ((Number) row[0]).longValue();
            BigDecimal prihod = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            prihodPoKodu.put(promoCodeId, prihod);
        }

        List<PromoCodeStatsDto> rezultat = new ArrayList<>();

        for (PromoCode kod : sviKodovi) {
            BigDecimal prihod = prihodPoKodu.getOrDefault(kod.getPromoCodeId(), BigDecimal.ZERO);
            int popust = kod.getDiscountPercent();

            // Kupac je platio originalCena * (1 - popust/100), znaci:
            // izgubili smo = prihod * popust / (100 - popust)
            BigDecimal estimatedValueLost;
            if (popust > 0 && popust < 100) {
                estimatedValueLost = prihod
                    .multiply(BigDecimal.valueOf(popust))
                    .divide(BigDecimal.valueOf(100 - popust), 2, RoundingMode.HALF_UP);
            } else {
                estimatedValueLost = BigDecimal.ZERO;
            }

            rezultat.add(PromoCodeStatsDto.builder()
                .promoCodeId(kod.getPromoCodeId())
                .code(kod.getCode())
                .discountPercent(popust)
                .active(kod.getActive())
                .validFrom(kod.getValidFrom())
                .validTo(kod.getValidTo())
                .usedCount(kod.getUsedCount())
                .maxUses(kod.getMaxUses())
                .totalRevenue(prihod)
                .estimatedValueLost(estimatedValueLost)
                .build());
        }

        return rezultat;
    }

    public ActionsAnalyticsResponse getActionsAnalytics(Long festivalId, User user) {
        requireSalesDirectorFestival(user, festivalId);

        List<BundleDeal> sveAkcije = bundleDealRepository
            .findByTicketType_Festival_FestivalIdOrderByVaziOdDesc(festivalId);

        // Prihod po akciji — sabira ukupnaCena svih kupovina u kojima je ta akcija iskoriscena
        Map<Long, BigDecimal> prihodPoAkciji = new java.util.HashMap<>();
        for (Object[] row : kupovinaRepository.revenueByBundleDealForFestival(festivalId)) {
            Long akcijaId = ((Number) row[0]).longValue();
            BigDecimal prihod = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            prihodPoAkciji.put(akcijaId, prihod);
        }

        List<BundleDealStatsDto> bundleDeals = new ArrayList<>();

        for (BundleDeal akcija : sveAkcije) {
            BigDecimal prihod = prihodPoAkciji.getOrDefault(akcija.getAkcijaId(), BigDecimal.ZERO);

            // Po svakoj kupovini: kupac plati za kupiKarata karata, dobija dobijaKarata gratis.
            // Cena jedne karte u toj kupovini = prihod / kupiKarata.
            // Izgubili smo = dobijaKarata * (prihod / kupiKarata) po kupovini.
            // Ukupno = prihod * dobijaKarata / kupiKarata (suma po svim kupovinama).
            BigDecimal estimatedValueLost;
            if (akcija.getKupiKarata() > 0) {
                estimatedValueLost = prihod
                    .multiply(BigDecimal.valueOf(akcija.getDobijaKarata()))
                    .divide(BigDecimal.valueOf(akcija.getKupiKarata()), 2, RoundingMode.HALF_UP);
            } else {
                estimatedValueLost = BigDecimal.ZERO;
            }

            bundleDeals.add(BundleDealStatsDto.builder()
                .akcijaId(akcija.getAkcijaId())
                .ticketTypeName(akcija.getTicketType().getName())
                .kupiKarata(akcija.getKupiKarata())
                .dobijaKarata(akcija.getDobijaKarata())
                .vaziOd(akcija.getVaziOd())
                .vaziDo(akcija.getVaziDo())
                .active(akcija.getActive())
                .applicationsCount(akcija.getUsedCount())
                .totalFreeTickets(akcija.getUsedCount() * akcija.getDobijaKarata())
                .totalRevenue(prihod)
                .estimatedValueLost(estimatedValueLost)
                .build());
        }

        List<DailyBundleImpactDto> dnevniUticaj = new ArrayList<>();
        for (Object[] row : kupovinaRepository.dailyBundleImpactForFestival(festivalId)) {
            dnevniUticaj.add(DailyBundleImpactDto.builder()
                .date((String) row[0])
                .purchasesCount(row[1] != null ? ((Number) row[1]).intValue() : 0)
                .revenue(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                .build());
        }

        return ActionsAnalyticsResponse.builder()
            .bundleDeals(bundleDeals)
            .dailyImpact(dnevniUticaj)
            .build();
    }

    public DynamicPricingAnalyticsResponse getDynamicPricingAnalytics(Long festivalId, User user) {
        requireSalesDirectorFestival(user, festivalId);

        // Dnevni podaci: ticketTypeId -> lista po danima
        Map<Long, List<DailyPriceSalesDto>> dnevniPoTipu = new java.util.HashMap<>();
        for (Object[] row : kupovinaRepository.dailyPriceSalesForFestival(festivalId)) {
            Long ttId      = ((Number) row[0]).longValue();
            String dan     = (String) row[1];
            int kolicina   = ((Number) row[2]).intValue();
            BigDecimal cena = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

            dnevniPoTipu
                .computeIfAbsent(ttId, k -> new java.util.ArrayList<>())
                .add(DailyPriceSalesDto.builder()
                    .date(dan)
                    .ticketsSold(kolicina)
                    .price(cena)
                    .build());
        }

        // Summary po tipu karte i ukupne sume
        BigDecimal ukupniPremium  = BigDecimal.ZERO;
        BigDecimal ukupniBaseline = BigDecimal.ZERO;
        List<DynamicPricingTicketTypeDto> tipoviKarata = new java.util.ArrayList<>();

        for (Object[] row : kupovinaRepository.dynamicPricingSummaryForFestival(festivalId)) {
            Long ttId             = ((Number) row[0]).longValue();
            String naziv          = (String) row[1];
            Boolean dpAktivan     = (Boolean) row[2];
            int ukupnoProdato     = ((Number) row[3]).intValue();
            BigDecimal baseline   = row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO;
            BigDecimal premium    = row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO;

            ukupniBaseline = ukupniBaseline.add(baseline);
            ukupniPremium  = ukupniPremium.add(premium);

            tipoviKarata.add(DynamicPricingTicketTypeDto.builder()
                .ticketTypeId(ttId)
                .name(naziv)
                .dynamicPricingActive(dpAktivan)
                .totalTicketsSold(ukupnoProdato)
                .baselineRevenue(baseline)
                .revenuePremium(premium)
                .dailyData(dnevniPoTipu.getOrDefault(ttId, List.of()))
                .build());
        }

        return DynamicPricingAnalyticsResponse.builder()
            .totalRevenuePremium(ukupniPremium)
            .totalBaselineRevenue(ukupniBaseline)
            .ticketTypes(tipoviKarata)
            .build();
    }

    private TicketType requireTicketTypeForSalesDirector(Long ticketTypeId, User user) {
        TicketType tt = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket type not found"));
        requireSalesDirectorFestival(user, tt.getFestival().getFestivalId());
        return tt;
    }

    private Festival requireSalesDirectorFestival(User user, Long festivalId) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sales Director access required");
        }
        if (!assignment.getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this festival");
        }
        return assignment.getFestival();
    }
}
