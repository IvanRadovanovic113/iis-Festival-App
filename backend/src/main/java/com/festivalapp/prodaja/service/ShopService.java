package com.festivalapp.prodaja.service;

import com.festivalapp.prodaja.dto.ShopTicketTypeResponse;
import com.festivalapp.prodaja.model.BundleDeal;
import com.festivalapp.prodaja.model.PricingPeriod;
import com.festivalapp.prodaja.repository.BundleDealRepository;
import com.festivalapp.prodaja.repository.PricingPeriodRepository;
import com.festivalapp.prodaja.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final TicketTypeRepository ticketTypeRepository;
    private final PricingPeriodRepository pricingPeriodRepository;
    private final BundleDealRepository bundleDealRepository;

    public List<ShopTicketTypeResponse> getAvailableTicketTypes() {
        LocalDate today = LocalDate.now();
        return ticketTypeRepository.findAll().stream()
            .filter(tt -> (tt.getTotalQuantity() - tt.getSoldCount()) > 0)
            .map(tt -> {
                PricingPeriod activePeriod = pricingPeriodRepository
                    .findActiveForTicketType(tt.getTicketTypeId(), today)
                    .orElse(null);
                List<BundleDeal> activeBundles = bundleDealRepository
                    .findActiveByTicketType(tt.getTicketTypeId(), today);
                return ShopTicketTypeResponse.from(tt, activePeriod, activeBundles);
            })
            .toList();
    }
}
