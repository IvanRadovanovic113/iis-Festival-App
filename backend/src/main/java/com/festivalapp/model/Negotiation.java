package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "negotiations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id", nullable = false)
    private Performer performer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by_user_id", nullable = false, updatable = false)
    private User startedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_state_id", nullable = false)
    private WorkflowState currentState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NegotiationStatus status;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @OneToMany(mappedBy = "negotiation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NegotiationStateHistory> history = new ArrayList<>();
}