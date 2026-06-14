package com.festivalapp.dto.eventorganization;

import jakarta.validation.constraints.Size;

public record ResolveTaskRequest(
    @Size(max = 1000)
    String note
) {
}
