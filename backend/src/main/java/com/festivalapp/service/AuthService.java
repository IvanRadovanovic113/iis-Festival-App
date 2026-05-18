package com.festivalapp.service;

import com.festivalapp.dto.LoginRequest;
import com.festivalapp.dto.LoginResponse;
import com.festivalapp.dto.UserDto;
import com.festivalapp.model.User;
import com.festivalapp.repository.UserRepository;
import com.festivalapp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow();
        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, UserDto.from(user));
    }
}
