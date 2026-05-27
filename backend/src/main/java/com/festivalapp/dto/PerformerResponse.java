package com.festivalapp.dto;

import com.festivalapp.model.Performer;
import com.festivalapp.model.PerformerStatus;
import com.festivalapp.model.PerformerType;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PerformerResponse {

    private Long performerId;
    private String stageName;
    private String firstName;
    private String lastName;
    private String genre;
    private Integer popularity;
    private Integer averageDurationMinutes;
    private String countryOfOrigin;
    private PerformerType performerType;
    private Integer numberOfMembers;
    private PerformerStatus status;
    private String bio;
    private LocalDateTime createdAt;

    public static PerformerResponse from(Performer performer) {
        PerformerResponse response = new PerformerResponse();
        response.performerId = performer.getPerformerId();
        response.stageName = performer.getStageName();
        response.firstName = performer.getFirstName();
        response.lastName = performer.getLastName();
        response.genre = performer.getGenre();
        response.popularity = performer.getPopularity();
        response.averageDurationMinutes = performer.getAverageDurationMinutes();
        response.countryOfOrigin = performer.getCountryOfOrigin();
        response.performerType = performer.getPerformerType();
        response.numberOfMembers = performer.getNumberOfMembers();
        response.status = performer.getStatus();
        response.bio = performer.getBio();
        response.createdAt = performer.getCreatedAt();
        return response;
    }
}