package com.festivalapp.service;

import com.festivalapp.dto.LoginRequest;
import com.festivalapp.dto.LoginResponse;
import com.festivalapp.dto.RegisterRequest;
import com.festivalapp.dto.UserDto;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.UserRepository;
import com.festivalapp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username()).orElseThrow();
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId()).orElse(null);
        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, UserDto.from(user, assignment));
    }

    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        return UserDto.from(userRepository.save(user), null);
    }

    public UserDto getMe(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId()).orElse(null);
        return UserDto.from(user, assignment);
    }
}
