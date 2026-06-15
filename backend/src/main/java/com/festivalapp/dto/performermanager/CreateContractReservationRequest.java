package com.festivalapp.dto.performermanager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateContractReservationRequest {

    @Valid
    @Size(max = 50)
    private List<ContractReservationResourceRequest> existingResources = new ArrayList<>();

    @Valid
    @Size(max = 50)
    private List<ContractReservationCustomResourceRequest> customResources = new ArrayList<>();

    @Size(max = 1000)
    private String notes;
}
