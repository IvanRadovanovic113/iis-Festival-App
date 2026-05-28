package com.festivalapp.dto;

import com.festivalapp.model.Offer;
import com.festivalapp.model.OfferStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OfferResponse {

    private Long offerId;
    private BigDecimal price;
    private LocalDateTime performanceDate;
    private String location;
    private OfferStatus status;
    private Long workflowTemplateId;

    public static OfferResponse from(Offer offer) {
        OfferResponse response = new OfferResponse();
        response.offerId = offer.getOfferId();
        response.price = offer.getPrice();
        response.performanceDate = offer.getPerformanceDate();
        response.location = offer.getLocation();
        response.status = offer.getStatus();
        response.workflowTemplateId = offer.getWorkflowTemplateId();
        return response;
    }
}