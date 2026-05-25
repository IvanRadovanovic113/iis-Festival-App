package com.festivalapp.prodaja.model;

import com.festivalapp.model.Festival;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketTypeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Builder.Default
    @Column(nullable = false)
    private Integer soldCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean dynamicPricingActive = false;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
