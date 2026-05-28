package com.festivalapp.dto;

import com.festivalapp.model.Offer;
import com.festivalapp.model.OfferStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OfferDetailResponse {

    private Long offerId;
    private BigDecimal price;
    private LocalDateTime performanceDate;
    private String location;
    private Integer durationMinutes;
    private OfferStatus status;
    private String additionalRequirements;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime frozenAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime archivedAt;
    private Long workflowTemplateId;
    private String createdByFullName;
    private List<PerformerShortInfo> interestedPerformers = new ArrayList<>();

    // Analitika (Privremeno hardkodovano do implementacije Epic-a za pregovore)
    private Integer negotiationCount;
    private Integer successfulContractsCount;
    private List<String> negotiations;

    @Getter
    @Setter
    public static class PerformerShortInfo {
        private Long performerId;
        private String stageName;
        private String genre;
        private String countryOfOrigin;
        private String performerType;
    }

    public static OfferDetailResponse from(Offer offer) {
        OfferDetailResponse response = new OfferDetailResponse();
        response.offerId = offer.getOfferId();
        response.price = offer.getPrice();
        response.performanceDate = offer.getPerformanceDate();
        response.location = offer.getLocation();
        response.durationMinutes = offer.getDurationMinutes();
        response.status = offer.getStatus();
        response.additionalRequirements = offer.getAdditionalRequirements();
        response.createdAt = offer.getCreatedAt();
        response.publishedAt = offer.getPublishedAt();
        response.frozenAt = offer.getFrozenAt();
        response.acceptedAt = offer.getAcceptedAt();
        response.archivedAt = offer.getArchivedAt();
        response.workflowTemplateId = offer.getWorkflowTemplateId();
        
        if (offer.getCreatedBy() != null) {
            response.createdByFullName = offer.getCreatedBy().getUsername();
        } else {
            response.createdByFullName = "System";
        }
        
        if (offer.getInterestedPerformers() != null) {
            response.interestedPerformers = offer.getInterestedPerformers().stream()
                .map(p -> {
                    PerformerShortInfo info = new PerformerShortInfo();
                    info.setPerformerId(p.getPerformerId());
                    info.setStageName(p.getStageName());
                    info.setGenre(p.getGenre());
                    info.setCountryOfOrigin(p.getCountryOfOrigin());
                    info.setPerformerType(p.getPerformerType().name());
                    return info;
                }).collect(Collectors.toList());
        }

        // Podrazumevane vrednosti za analitiku
        response.negotiationCount = 0;
        response.successfulContractsCount = 0;
        response.negotiations = new ArrayList<>();
        
        return response;
    }
}