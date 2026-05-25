package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "karte")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Karta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kartaId;

    @ManyToOne
    @JoinColumn(name = "kupovina_id", nullable = false)
    private Kupovina kupovina;

    @Column(nullable = false, unique = true, length = 500)
    private String qrKod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KartaStatus status = KartaStatus.AKTIVNA;
}
