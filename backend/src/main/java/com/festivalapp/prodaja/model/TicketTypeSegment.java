package com.festivalapp.prodaja.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_type_segments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @ManyToOne
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;
}
