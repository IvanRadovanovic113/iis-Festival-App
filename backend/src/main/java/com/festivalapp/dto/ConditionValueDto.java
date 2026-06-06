package com.festivalapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionValueDto {

    @NotNull(message = "Condition ID is required.")
    private Long conditionId;

    @NotNull(message = "Value cannot be null.")
    private String value;
}