package com.hospital.history.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDTO {
    private UUID id;
    private String nome;
    private String email;
    private String role;
}
