package com.festivalapp.dto;

import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;

public record UserDto(Long id, String username, String email, String role, AssignmentDto assignment) {

    public record AssignmentDto(Long festivalId, String festivalNaziv, String festivalRole) {}

    public static UserDto from(User user, UserFestivalAssignment assignment) {
        AssignmentDto assignmentDto = assignment == null ? null :
            new AssignmentDto(
                assignment.getFestival().getFestivalId(),
                assignment.getFestival().getNaziv(),
                assignment.getRole().name()
            );
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().name() : null,
            assignmentDto
        );
    }
}
