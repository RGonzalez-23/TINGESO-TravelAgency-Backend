package com.example.TINGESO.Controllers;

import com.example.TINGESO.DTOs.ReservationResponseDTO;
import com.example.TINGESO.Services.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    public void getMyReservations_ShouldReturnList() throws Exception {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(1L);
        dto.setKeycloakUserId("user1");
        given(reservationService.getMyReservations("user1")).willReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/reservations/me")
                .with(jwt().jwt(builder -> builder.subject("user1").claim("realm_access", java.util.Map.of("roles", java.util.List.of("CLIENTE"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void cancelReservation_ShouldReturnOk() throws Exception {
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("newStatus", "CANCELADA");

        mockMvc.perform(put("/api/reservations/1/status") // Real endpoint: /{id}/status
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload))
                        .with(jwt().jwt(builder -> builder
                                .subject("user123") // This will be the userId that the service will receive
                                .claim("realm_access", java.util.Map.of("roles", java.util.List.of("CLIENTE"))))))
                .andExpect(status().isOk());
    }
}
