package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation_condition_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationConditionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id", nullable = false)
    private TransitionCondition condition;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private LocalDateTime enteredAt;
}