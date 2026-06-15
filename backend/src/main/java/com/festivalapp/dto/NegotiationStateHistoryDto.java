package com.festivalapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NegotiationStateHistoryDto {
    private String stateName;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
}