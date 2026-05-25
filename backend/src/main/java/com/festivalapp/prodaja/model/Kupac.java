package com.festivalapp.prodaja.model;

import com.festivalapp.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kupci")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kupac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kupacId;

    @Column(nullable = false)
    private String ime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KupacTier tier = KupacTier.STANDARD;

    @Builder.Default
    @Column(nullable = false)
    private Integer ukupnoKupovina = 0;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
