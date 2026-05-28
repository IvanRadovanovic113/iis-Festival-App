package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "performance_date", nullable = false)
    private LocalDateTime performanceDate;

    @Column(nullable = false)
    private String location;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(name = "additional_requirements", length = 2000, nullable = true)
    private String additionalRequirements;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "frozen_at")
    private LocalDateTime frozenAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "workflow_template_id", nullable = false)
    private Long workflowTemplateId;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "offer_interested_performers",
        joinColumns = @JoinColumn(name = "offer_id"),
        inverseJoinColumns = @JoinColumn(name = "performer_id")
    )
    @Builder.Default
    private List<Performer> interestedPerformers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OfferStatus.DRAFT;
        }
    }
}