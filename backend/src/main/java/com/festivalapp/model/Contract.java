package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by_user_id", nullable = false)
    private User signedBy;

    // Snapshot podataka u trenutku potpisivanja
    @Column(name = "condition_snapshot_json", columnDefinition = "TEXT", nullable = false)
    private String conditionSnapshotJson;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @PrePersist
    protected void onCreate() {
        this.signedAt = LocalDateTime.now();
    }
}