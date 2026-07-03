package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriticalNegotiationDTO {
    private Long negotiationId;
    private String performerName;
    private Double overdueHours;
    private LocalDateTime deadline;
}