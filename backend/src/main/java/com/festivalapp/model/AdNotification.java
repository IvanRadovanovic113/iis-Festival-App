package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ad_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String message;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
