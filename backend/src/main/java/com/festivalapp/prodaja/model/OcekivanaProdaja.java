package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ocekivana_prodaja")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcekivanaProdaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ocekivanaProdajaId;

    @OneToOne
    @JoinColumn(name = "pricing_period_id", nullable = false, unique = true)
    private PricingPeriod pricingPeriod;

    @Column(nullable = false)
    private Integer brojKarata;

    @Column(nullable = false)
    private Integer brojSati;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal agresivnost;

    @Column(nullable = false)
    private Integer intervalMinuti;

    @Column(nullable = false)
    private Integer scarcityPragNizak;

    @Column(nullable = false)
    private Integer scarcityPragVisok;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scarcityMultiplikatorNizak;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scarcityMultiplikatorVisok;
}
