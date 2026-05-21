package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "festivals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long festivalId;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false)
    private String lokacija;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FestivalStatus status;

    @Column(nullable = false)
    private LocalDate datumPocetka;

    @Column(nullable = false)
    private LocalDate datumZavrsetka;
}
