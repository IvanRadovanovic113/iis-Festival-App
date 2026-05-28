package com.festivalapp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OfferRequest {

    @NotNull(message = "Price is a required field.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Performance date is a required field.")
    @Future(message = "Performance date must be in the future.")
    private LocalDateTime performanceDate;

    @NotBlank(message = "Location is a required field.")
    @Size(max = 255)
    private String location;

    @NotNull(message = "Duration is a required field.")
    @Min(value = 1, message = "Duration must be positive.")
    private Integer durationMinutes;

    @Size(max = 2000)
    private String additionalRequirements;

    @NotNull(message = "Workflow template is a required field.")
    private Long workflowTemplateId;
}