package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_festival_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFestivalAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
