package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cenovna_istorija")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CenovnaIstorija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cenovnaIstorijaId;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @ManyToOne
    @JoinColumn(name = "pricing_period_id", nullable = false)
    private PricingPeriod pricingPeriod;

    @Column(nullable = false)
    private LocalDateTime datum;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal staraCena;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal novaCena;

    @Column(nullable = false, length = 200)
    private String razlog;

    @Builder.Default
    @Column(nullable = false)
    private Boolean jeRucnaPromena = false;
}
