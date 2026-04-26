package com.example.TINGESO.Controllers;

import com.example.TINGESO.DTOs.ReservationRequestDTO;
import com.example.TINGESO.DTOs.PaymentRequestDTO;
import com.example.TINGESO.Services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // Extraemos el UUID real del usuario desde los tokens de Keycloak inyectados por Spring Security
    private String getUserIdFromJwt(Jwt jwt) {
        return jwt.getClaimAsString("sub"); 
    }

    // Crear una reserva (Solo usuarios logueados automáticamente inyecta el DTO y el JWT)
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Se requiere autenticación provista por IAM");
        }

        try {
            String userId = getUserIdFromJwt(jwt);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Pagar una reserva PENDIENTE
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payReservation(
            @PathVariable Long id,
            @RequestBody PaymentRequestDTO req,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.ok(reservationService.payReservation(id, req, getUserIdFromJwt(jwt)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Obtener reservas de mi propio usuario
    @GetMapping("/me")
    public ResponseEntity<?> getMyReservations(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reservationService.getMyReservations(getUserIdFromJwt(jwt)));
    }
}
