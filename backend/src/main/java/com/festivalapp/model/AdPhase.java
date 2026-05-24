package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ad_phases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long phaseId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean emailNotification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role assignedRole;
}
