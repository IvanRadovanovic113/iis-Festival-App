package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transition_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitionCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "condition_key", nullable = false, unique = true)
    private String conditionKey;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private ConditionDataType dataType;

    @Column(nullable = false)
    private boolean necessary = false;
}