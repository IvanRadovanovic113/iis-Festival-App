package com.festivalapp.controller;

import com.festivalapp.dto.AssignmentRequest;
import com.festivalapp.dto.UserDto;
import com.festivalapp.model.User;
import com.festivalapp.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user, null));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{id}/assignment")
    public ResponseEntity<UserDto> assign(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(adminService.assign(id, request));
    }

    @PutMapping("/users/{id}/assignment")
    public ResponseEntity<UserDto> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(adminService.assign(id, request));
    }

    @DeleteMapping("/users/{id}/assignment")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        adminService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        return ResponseEntity.ok(adminService.getAvailableRoles());
    }
}
