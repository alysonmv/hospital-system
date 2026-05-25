package com.hospital.history.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MedicalHistoryDTO {
    private UUID id;
    private String descricao;
    private String dataRegistro;
    private PatientDTO paciente;
}
