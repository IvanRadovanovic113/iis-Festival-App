package com.festivalapp.dto;

import com.festivalapp.prodaja.model.Kupac;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;

public record UserDto(Long id, String username, String email, String role,
                      AssignmentDto assignment, KupacDto buyer) {

    public record AssignmentDto(Long festivalId, String festivalName, String festivalRole) {}

    public record KupacDto(Long kupacId, String ime, String tier, Integer ukupnoKupovina) {}

    public static UserDto from(User user, UserFestivalAssignment assignment, Kupac kupac) {
        AssignmentDto assignmentDto = assignment == null ? null :
            new AssignmentDto(
                assignment.getFestival().getFestivalId(),
                assignment.getFestival().getName(),
                assignment.getRole().name()
            );
        KupacDto kupacDto = kupac == null ? null :
            new KupacDto(
                kupac.getKupacId(),
                kupac.getIme(),
                kupac.getTier().name(),
                kupac.getUkupnoKupovina()
            );
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().name() : null,
            assignmentDto,
            kupacDto
        );
    }

    /** Convenience overload for non-buyer users */
    public static UserDto from(User user, UserFestivalAssignment assignment) {
        return from(user, assignment, null);
    }
}
