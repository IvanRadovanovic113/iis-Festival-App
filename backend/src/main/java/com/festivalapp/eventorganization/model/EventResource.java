package com.festivalapp.eventorganization.model;

import com.festivalapp.model.Festival;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_resources")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer totalQuantity;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
