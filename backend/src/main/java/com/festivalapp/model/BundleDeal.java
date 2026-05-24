package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "bundle_deals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long akcijaId;

    @Column(nullable = false)
    private Integer kupiKarata;

    @Column(nullable = false)
    private Integer dobijaKarata;

    @Column(nullable = false)
    private LocalDate vaziOd;

    @Column(nullable = false)
    private LocalDate vaziDo;

    @Column(nullable = false)
    private Integer dostupnoAkcija;

    @Builder.Default
    @Column(nullable = false)
    private Integer usedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;
}
