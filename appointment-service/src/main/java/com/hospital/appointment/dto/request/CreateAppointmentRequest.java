package com.hospital.appointment.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(

        @NotNull(message = "Data da consulta é obrigatória")
        @Future(message = "Data da consulta deve ser no futuro")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataConsulta,

        @NotNull(message = "Médico é obrigatório")
        UUID medicoId,

        @NotNull(message = "Paciente é obrigatório")
        UUID pacienteId,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao
) {}
