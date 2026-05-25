package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "ad_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ad_versions_ad_version", columnNames = {"ad_id", "version_number"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String contentValue;

    @Column(nullable = false)
    private LocalDate changedAt;

    @Column(nullable = false)
    private String phaseName;
}
