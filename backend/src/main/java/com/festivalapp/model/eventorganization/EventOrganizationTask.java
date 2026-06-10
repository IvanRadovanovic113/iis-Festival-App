package com.festivalapp.model.eventorganization;

import com.festivalapp.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "event_organization_tasks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"request_resource_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventOrganizationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "request_resource_id", nullable = false)
    private RequestResource requestResource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventOrganizationTaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventOrganizationTaskStatus status;

    private String title;

    private String performerName;

    private String stageName;

    private java.time.LocalDate deadline;

    @Column(length = 1000)
    private String resolutionNote;

    @Column(length = 1000)
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedBy;

    @ManyToOne
    @JoinColumn(name = "rejected_by_user_id")
    private User rejectedBy;

    private LocalDateTime resolvedAt;

    private LocalDateTime rejectedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = EventOrganizationTaskStatus.OPEN;
        }
    }
}
