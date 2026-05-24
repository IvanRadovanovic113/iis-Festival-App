package com.festivalapp.dto;

import com.festivalapp.model.AdPhase;
import com.festivalapp.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdPhaseResponse {

    private Long phaseId;
    private String name;
    private String description;
    private Integer orderIndex;
    private boolean emailNotification;
    private Role assignedRole;

    public static AdPhaseResponse from(AdPhase phase) {
        AdPhaseResponse response = new AdPhaseResponse();
        response.phaseId = phase.getPhaseId();
        response.name = phase.getName();
        response.description = phase.getDescription();
        response.orderIndex = phase.getOrderIndex();
        response.emailNotification = phase.isEmailNotification();
        response.assignedRole = phase.getAssignedRole();
        return response;
    }
}
