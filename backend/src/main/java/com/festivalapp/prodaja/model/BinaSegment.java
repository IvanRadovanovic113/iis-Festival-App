package com.festivalapp.prodaja.model;

import com.festivalapp.model.Bina;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "bina_segmenti",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bina_id", "segment_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinaSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bina_id", nullable = false)
    private Bina bina;

    @ManyToOne
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Column(nullable = false)
    private Integer kapacitet;
}
