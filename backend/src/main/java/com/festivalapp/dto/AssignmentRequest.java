package com.festivalapp.dto;

import com.festivalapp.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentRequest {

    @NotNull
    private Long festivalId;

    @NotNull
    private Role role;
}
