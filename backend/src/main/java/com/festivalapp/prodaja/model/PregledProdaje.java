package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pregled_prodaje")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PregledProdaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pregledProdajeId;

    @ManyToOne
    @JoinColumn(name = "pricing_period_id", nullable = false)
    private PricingPeriod pricingPeriod;

    @Column(nullable = false)
    private LocalDateTime datum;

    @Column(nullable = false)
    private Integer prodajeUPerioduU;

    @Column(nullable = false)
    private Integer ocekivanoPeriodU;
}
