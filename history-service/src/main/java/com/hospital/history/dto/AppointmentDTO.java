package com.hospital.history.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AppointmentDTO {
    private UUID id;
    private String dataConsulta;
    private String status;
    private String descricao;
    private UserDTO medico;
    private PatientDTO paciente;
    private String createdAt;
    private String updatedAt;
}
