package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_transitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_state_id", nullable = false)
    private WorkflowState sourceState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_state_id", nullable = false)
    private WorkflowState targetState;

    @ManyToMany
    @JoinTable(
        name = "transition_condition_mapping",
        joinColumns = @JoinColumn(name = "transition_id"),
        inverseJoinColumns = @JoinColumn(name = "condition_id")
    )
    @Builder.Default
    private List<TransitionCondition> conditions = new ArrayList<>();
}