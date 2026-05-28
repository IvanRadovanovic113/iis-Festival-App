package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_promotions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false, unique = true)
    private Ad ad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionChannel channel;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerDay;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;
}
