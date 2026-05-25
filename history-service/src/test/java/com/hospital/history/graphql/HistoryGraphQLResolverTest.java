package com.hospital.history.graphql;

import com.hospital.history.dto.AppointmentDTO;
import com.hospital.history.dto.MedicalHistoryDTO;
import com.hospital.history.dto.PatientDTO;
import com.hospital.history.service.HistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(HistoryGraphQLResolver.class)
@DisplayName("HistoryGraphQLResolver Tests")
class HistoryGraphQLResolverTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockBean
    HistoryService historyService;

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("buscarHistoricoPaciente should return list of MedicalHistoryDTO")
    void shouldFetchMedicalHistory() {
        UUID pacienteId = UUID.randomUUID();

        PatientDTO patientDTO = PatientDTO.builder()
                .id(pacienteId)
                .nome("João")
                .telefone("(48) 99999-0000")
                .build();

        MedicalHistoryDTO historyDTO = MedicalHistoryDTO.builder()
                .id(UUID.randomUUID())
                .descricao("Consulta de rotina")
                .dataRegistro("2024-01-10T09:00:00")
                .paciente(patientDTO)
                .build();

        when(historyService.buscarHistoricoPaciente(any())).thenReturn(List.of(historyDTO));

        graphQlTester.document("""
                query {
                    buscarHistoricoPaciente(pacienteId: "%s") {
                        id
                        descricao
                        dataRegistro
                        paciente { nome }
                    }
                }
                """.formatted(pacienteId))
                .execute()
                .path("buscarHistoricoPaciente")
                .entityList(MedicalHistoryDTO.class)
                .hasSize(1);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("listarConsultasFuturas should return future appointments")
    void shouldListFutureAppointments() {
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(UUID.randomUUID())
                .dataConsulta("2026-12-01T10:00:00")
                .status("AGENDADA")
                .build();

        when(historyService.listarConsultasFuturas(any())).thenReturn(List.of(dto));

        graphQlTester.document("""
                query {
                    listarConsultasFuturas {
                        id
                        dataConsulta
                        status
                    }
                }
                """)
                .execute()
                .path("listarConsultasFuturas")
                .entityList(AppointmentDTO.class)
                .hasSize(1);
    }
}
