package com.hospital.appointment.service;

import com.hospital.appointment.dto.request.LoginRequest;
import com.hospital.appointment.dto.response.AuthResponse;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.UserRepository;
import com.hospital.appointment.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var token = jwtService.generateToken(user);
        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.of(token, user.getId(), user.getNome(), user.getEmail(), user.getRole());
    }
}
