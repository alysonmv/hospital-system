package com.hospital.appointment.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hospital.appointment.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateAppointmentRequest(

        @Future(message = "Data da consulta deve ser no futuro")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataConsulta,

        AppointmentStatus status,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao
) {}
