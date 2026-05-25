package com.hospital.appointment.dto.response;

import com.hospital.appointment.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        LocalDateTime dataConsulta,
        AppointmentStatus status,
        String descricao,
        UserSummary medico,
        PatientSummary paciente,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record UserSummary(UUID id, String nome, String email) {}
    public record PatientSummary(UUID id, String nome, String telefone) {}
}
