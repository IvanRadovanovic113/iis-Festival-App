package com.festivalapp.dto.eventorganization;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestResourceRequest {

    private Long resourceId;

    @Size(max = 255)
    private String requestedName;

    @Size(max = 255)
    private String requestedType;

    @NotNull
    @Min(1)
    private Integer quantity;
}
