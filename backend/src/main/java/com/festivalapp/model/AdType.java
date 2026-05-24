package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ad_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adTypeId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String contentType;

    @ManyToMany
    @JoinTable(
        name = "ad_type_phases",
        joinColumns = @JoinColumn(name = "ad_type_id"),
        inverseJoinColumns = @JoinColumn(name = "phase_id")
    )
    @OrderColumn(name = "sequence_index")
    @Builder.Default
    private List<AdPhase> phases = new ArrayList<>();
}
