package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_locks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kupac_id", nullable = false)
    private Kupac kupac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_deal_id")
    private BundleDeal bundleDeal;

    @Column(nullable = false)
    private Integer lockedQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lockedPricePerTicket;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lockedBaseTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lockedFinalPrice;

    @Column(nullable = false)
    private int lockedTotalTickets;

    @Column(nullable = false)
    private int lockedFreeTickets;

    @Column(nullable = false)
    private int lockedPromoDiscountPct;

    @Column(nullable = false)
    private int lockedTierDiscountPct;

    @Column(nullable = false)
    private int lockedTotalDiscountPct;

    @Column(nullable = false)
    private int lockedBundleApplications;

    @Column(nullable = false)
    private LocalDateTime lockedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;
}
