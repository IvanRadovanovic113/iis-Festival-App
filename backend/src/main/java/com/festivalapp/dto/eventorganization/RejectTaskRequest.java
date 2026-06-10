package com.festivalapp.dto.eventorganization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectTaskRequest(
    @NotBlank
    @Size(max = 1000)
    String reason
) {
}
