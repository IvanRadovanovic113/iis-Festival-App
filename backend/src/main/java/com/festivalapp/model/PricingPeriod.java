package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pricing_periods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pricingPeriodId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Builder.Default
    @Column(nullable = false)
    private Boolean dynamicPricingActive = false;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;
}
