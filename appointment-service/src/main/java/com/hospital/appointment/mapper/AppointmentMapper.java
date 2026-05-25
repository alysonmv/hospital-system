package com.hospital.appointment.mapper;

import com.hospital.appointment.domain.entity.Appointment;
import com.hospital.appointment.dto.response.AppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppointmentMapper {

    @Mapping(target = "medico.id", source = "medico.id")
    @Mapping(target = "medico.nome", source = "medico.nome")
    @Mapping(target = "medico.email", source = "medico.email")
    @Mapping(target = "paciente.id", source = "paciente.id")
    @Mapping(target = "paciente.nome", source = "paciente.nome")
    @Mapping(target = "paciente.telefone", source = "paciente.telefone")
    AppointmentResponse toResponse(Appointment appointment);
}
