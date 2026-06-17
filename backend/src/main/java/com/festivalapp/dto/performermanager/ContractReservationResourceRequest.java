package com.festivalapp.dto.performermanager;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractReservationResourceRequest {

    @NotNull
    private Long resourceId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
