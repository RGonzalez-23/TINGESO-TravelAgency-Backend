package com.example.TINGESO.Controllers;

import com.example.TINGESO.DTOs.IamProfileUpdateRequest;
import com.example.TINGESO.Services.KeycloakAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam")
@CrossOrigin(origins = "*")
public class IamController {

    private final KeycloakAdminService keycloakAdminService;

    public IamController(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        try {
            return ResponseEntity.ok(keycloakAdminService.getCurrentUserProfile(jwt.getSubject()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMine(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody IamProfileUpdateRequest request
    ) {
        try {
            return ResponseEntity.ok(keycloakAdminService.updateOwnProfile(jwt.getSubject(), request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAsAdmin(
            @PathVariable String userId,
            @Valid @RequestBody IamProfileUpdateRequest request
    ) {
        try {
            return ResponseEntity.ok(keycloakAdminService.adminUpdateUser(userId, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/admin/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable String userId) {
        try {
            keycloakAdminService.deactivateUser(userId);
            return ResponseEntity.ok("Usuario desactivado en Keycloak");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
