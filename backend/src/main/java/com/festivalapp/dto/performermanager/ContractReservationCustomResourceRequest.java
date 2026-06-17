package com.festivalapp.dto.performermanager;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractReservationCustomResourceRequest {

    @NotBlank
    @Size(max = 255)
    private String requestedName;

    @NotBlank
    @Size(max = 255)
    private String requestedType;

    @NotNull
    @Min(1)
    private Integer quantity;
}
