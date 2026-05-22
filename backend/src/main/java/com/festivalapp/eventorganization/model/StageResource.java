package com.festivalapp.eventorganization.model;

import com.festivalapp.model.Stage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "stage_resources",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stage_id", "resource_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private EventResource resource;

    @Column(nullable = false)
    private Integer quantity;
}
