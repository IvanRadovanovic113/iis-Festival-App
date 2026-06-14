package com.festivalapp.model.eventorganization;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_resources")
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
    @JoinColumn(name = "resource_id")
    private EventResource resource;

    @Column(name = "requested_name")
    private String requestedName;

    @Column(name = "requested_type")
    private String requestedType;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestResourceStatus status;
}
