package com.festivalapp.dto;

import com.festivalapp.model.PerformerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformerRequest {

    @NotBlank(message = "Stage name is a required field.")
    @Size(max = 255)
    private String stageName;

    private String firstName;

    private String lastName;

    @NotBlank(message = "Genre is a required field.")
    private String genre;

    private Integer popularity;

    @Min(value = 1, message = "Average duration must be positive.")
    private Integer averageDurationMinutes;

    @NotBlank(message = "Country of origin is a required field.")
    private String countryOfOrigin;

    @NotNull(message = "Type of performer(SOLO/BAND) is a required field.")
    private PerformerType performerType;

    @NotNull(message = "Number of members is a required field.")
    @Min(value = 1, message = "Number of members must be positive.")
    private Integer numberOfMembers;

    @Size(max = 2000)
    private String bio;
}