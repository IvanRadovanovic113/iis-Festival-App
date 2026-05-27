package com.festivalapp.eventorganization.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "request_resources",
    uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_request_id", "resource_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_request_id", nullable = false)
    private EventReservationRequest reservationRequest;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private EventResource resource;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestResourceStatus status;
}
