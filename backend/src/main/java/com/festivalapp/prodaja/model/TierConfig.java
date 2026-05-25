package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tier_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierConfig {

    /** BRONZE, SILVER ili GOLD — STANDARD nema config red (popust = 0) */
    @Id
    @Enumerated(EnumType.STRING)
    private KupacTier tier;

    /** Minimalan broj kupljenih karata za dostizanje ovog tiera */
    @Column(nullable = false)
    private Integer minTickets;

    /** Popust u procentima koji ovaj tier daje */
    @Column(nullable = false)
    private Integer discountPercent;
}
