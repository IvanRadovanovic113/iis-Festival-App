package com.festivalapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ads")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentFileName;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "content_storage_path", columnDefinition = "TEXT")
    private String contentStoragePath;

    @Column(name = "content_original_file_name")
    private String contentOriginalFileName;

    @Column(name = "content_mime_type")
    private String contentMimeType;

    @Column(name = "content_size")
    private Long contentSize;

    @Column(nullable = false)
    private LocalDate lastChangeDate;

    @Column(nullable = false)
    private Integer versionNumber;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(name = "ad_type_id", nullable = false)
    private AdType adType;

    @ManyToOne
    @JoinColumn(name = "phase_id", nullable = false)
    private AdPhase currentPhase;

    @ManyToOne
    @JoinColumn(name = "last_edited_by_user_id")
    private User lastEditedByUser;

    @ManyToOne
    @JoinColumn(name = "last_edited_phase_id")
    private AdPhase lastEditedPhase;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_edited_role")
    private Role lastEditedRole;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @OneToOne(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AdPromotion promotion;
}
