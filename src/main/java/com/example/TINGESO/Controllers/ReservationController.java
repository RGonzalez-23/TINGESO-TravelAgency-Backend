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

    // We extract the real UUID of the user from the Keycloak tokens injected by Spring Security
    private String getUserIdFromJwt(Jwt jwt) {
        return jwt.getClaimAsString("sub"); 
    }

    // Create a reservation (Only logged-in users automatically inject the DTO and JWT)
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

    // Pay a PENDING reservation
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

    // Get reservations of my own user
    @GetMapping("/me")
    public ResponseEntity<?> getMyReservations(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reservationService.getMyReservations(getUserIdFromJwt(jwt)));
    }

    // [ADMIN] Get ALL system reservations
    @GetMapping("/all")
    public ResponseEntity<?> getAllReservations(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    // Change reservation status (Client confirming or Admin managing)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long id, 
            @RequestBody java.util.Map<String, String> payload, 
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        String newStatus = payload.get("newStatus");
        boolean isAdmin = false;
        try {
            java.util.Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") != null) {
                java.util.List<String> roles = (java.util.List<String>) realmAccess.get("roles");
                isAdmin = roles.contains("ADMIN");
            }
        } catch (Exception e) { /* Fail safe */ }

        try {
            return ResponseEntity.ok(reservationService.updateReservationStatus(id, newStatus, getUserIdFromJwt(jwt), isAdmin));
        } catch(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
