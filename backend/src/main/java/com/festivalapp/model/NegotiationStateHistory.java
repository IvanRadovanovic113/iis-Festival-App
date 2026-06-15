package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation_state_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationStateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private WorkflowState state;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;
}