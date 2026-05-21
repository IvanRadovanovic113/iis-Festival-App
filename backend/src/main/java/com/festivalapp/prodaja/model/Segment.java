package com.festivalapp.prodaja.model;

import com.festivalapp.model.Festival;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "segmenti")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Segment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long segmentId;

    @Column(nullable = false)
    private String naziv;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
