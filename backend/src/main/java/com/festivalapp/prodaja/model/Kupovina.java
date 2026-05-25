package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kupovine")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kupovina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kupovinaId;

    @ManyToOne
    @JoinColumn(name = "kupac_id", nullable = false)
    private Kupac kupac;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    /** Promo kod koji je iskorišćen — null ako nije primenjen */
    @ManyToOne
    @JoinColumn(name = "promo_code_id", nullable = true)
    private PromoCode promoCode;

    /** Bundle deal koji je iskorišćen — null ako nije primenjen */
    @ManyToOne
    @JoinColumn(name = "akcija_id", nullable = true)
    private BundleDeal bundleDeal;

    @Column(nullable = false)
    private LocalDateTime datum;

    /** Ukupan broj karata uključujući gratis karte */
    @Column(nullable = false)
    private Integer kolicina;

    /** Finalna cena nakon svih popusta */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ukupnaCena;
}
