package com.hospital.history.graphql;

import com.hospital.history.dto.AppointmentDTO;
import com.hospital.history.dto.MedicalHistoryDTO;
import com.hospital.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HistoryGraphQLResolver {

    private final HistoryService historyService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<MedicalHistoryDTO> buscarHistoricoPaciente(@Argument String pacienteId) {
        log.debug("GraphQL: buscarHistoricoPaciente - pacienteId={}", pacienteId);
        return historyService.buscarHistoricoPaciente(UUID.fromString(pacienteId));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<AppointmentDTO> listarConsultasFuturas(@Argument String pacienteId) {
        log.debug("GraphQL: listarConsultasFuturas - pacienteId={}", pacienteId);
        UUID pid = pacienteId != null ? UUID.fromString(pacienteId) : null;
        return historyService.listarConsultasFuturas(pid);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<AppointmentDTO> listarConsultasPassadas(@Argument String pacienteId) {
        log.debug("GraphQL: listarConsultasPassadas - pacienteId={}", pacienteId);
        UUID pid = pacienteId != null ? UUID.fromString(pacienteId) : null;
        return historyService.listarConsultasPassadas(pid);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public AppointmentDTO buscarConsultaPorId(@Argument String id) {
        log.debug("GraphQL: buscarConsultaPorId - id={}", id);
        return historyService.buscarConsultaPorId(UUID.fromString(id));
    }
}
