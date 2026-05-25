package com.festivalapp.prodaja.model;

import com.festivalapp.model.Festival;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "segments")
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
    private String name;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
