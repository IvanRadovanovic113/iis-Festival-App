package com.festivalapp.dto;

import com.festivalapp.model.User;

public record UserDto(Long id, String username, String email, String role) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
