package com.festivalapp.prodaja.model;

import com.festivalapp.model.Festival;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stageId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String location;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
