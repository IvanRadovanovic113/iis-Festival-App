package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bine")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long binaId;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false)
    private Integer kapacitet;

    @Column(nullable = false)
    private String lokacija;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
