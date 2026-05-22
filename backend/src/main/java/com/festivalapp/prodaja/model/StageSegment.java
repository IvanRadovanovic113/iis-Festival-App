package com.festivalapp.prodaja.model;

import com.festivalapp.model.Segment;
import com.festivalapp.model.Stage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "stage_segments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stage_id", "segment_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Column(nullable = false)
    private Integer capacity;
}
