package com.hospital.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.dto.request.LoginRequest;
import com.hospital.appointment.dto.response.AuthResponse;
import com.hospital.appointment.domain.enums.Role;
import com.hospital.appointment.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 with token")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("medico@hospital.com", "Hospital@123");
        AuthResponse response = AuthResponse.of(
                "eyJhbGciOiJIUzI1NiJ9.test",
                UUID.randomUUID(),
                "Dr. Silva",
                "medico@hospital.com",
                Role.ROLE_MEDICO
        );

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("medico@hospital.com"))
                .andExpect(jsonPath("$.role").value("ROLE_MEDICO"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 400 when email is blank")
    void shouldReturn400WhenEmailBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "senha");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
