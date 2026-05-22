package com.festivalapp.service;

import com.festivalapp.dto.AssignmentRequest;
import com.festivalapp.dto.UserDto;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.FestivalRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final FestivalRepository festivalRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getRole() != Role.ADMIN)
            .toList();
        Map<Long, UserFestivalAssignment> assignmentMap = assignmentRepository.findAll()
            .stream().collect(Collectors.toMap(a -> a.getUser().getId(), a -> a));
        return users.stream()
            .map(u -> UserDto.from(u, assignmentMap.get(u.getId())))
            .toList();
    }

    @Transactional
    public UserDto assign(Long userId, AssignmentRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign a festival to an admin");
        }
        Festival festival = festivalRepository.findById(request.getFestivalId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found"));

        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(userId)
            .orElse(UserFestivalAssignment.builder().user(user).build());
        assignment.setFestival(festival);
        assignment.setRole(request.getRole());
        return UserDto.from(user, assignmentRepository.save(assignment));
    }

    @Transactional
    public void deleteAssignment(Long userId) {
        if (!assignmentRepository.existsByUser_Id(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        }
        assignmentRepository.deleteByUser_Id(userId);
    }

    public List<String> getAvailableRoles() {
        return Arrays.stream(Role.values())
            .filter(r -> r != Role.ADMIN)
            .map(Role::name)
            .toList();
    }
}
