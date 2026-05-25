package com.hospital.appointment.dto.response;

import com.hospital.appointment.domain.enums.Role;

import java.util.UUID;

public record AuthResponse(
        String token,
        String type,
        UUID userId,
        String nome,
        String email,
        Role role
) {
    public static AuthResponse of(String token, UUID userId, String nome, String email, Role role) {
        return new AuthResponse(token, "Bearer", userId, nome, email, role);
    }
}
