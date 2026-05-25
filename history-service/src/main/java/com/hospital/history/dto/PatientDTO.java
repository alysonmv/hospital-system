package com.hospital.history.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PatientDTO {
    private UUID id;
    private String nome;
    private String dataNascimento;
    private String telefone;
}
