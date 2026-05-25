package com.hospital.appointment.controller;

import com.hospital.appointment.domain.enums.AppointmentStatus;
import com.hospital.appointment.dto.request.CreateAppointmentRequest;
import com.hospital.appointment.dto.request.UpdateAppointmentRequest;
import com.hospital.appointment.dto.response.AppointmentResponse;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Gerenciamento de consultas médicas")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    @Operation(summary = "Criar consulta")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    @Operation(summary = "Atualizar consulta")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    @Operation(summary = "Cancelar consulta")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    @Operation(summary = "Buscar consulta por ID")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    @Operation(summary = "Listar consultas")
    public ResponseEntity<Page<AppointmentResponse>> findAll(
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20, sort = "dataConsulta") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findAll(medicoId, pacienteId, status, pageable));
    }
}
