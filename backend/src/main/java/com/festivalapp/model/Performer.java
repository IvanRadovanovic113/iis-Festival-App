package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "performers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performerId;

    @Column(nullable = false)
    private String stageName;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private Integer popularity;

    @Column(name = "average_duration_minutes", nullable = false)
    private Integer averageDurationMinutes;

    @Column(nullable = false)
    private String countryOfOrigin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformerType performerType;

    @Column(nullable = false)
    private Integer numberOfMembers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformerStatus status;

    @Column(length = 2000, nullable = true)
    private String bio;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PerformerStatus.ACTIVE;
        }
    }
}