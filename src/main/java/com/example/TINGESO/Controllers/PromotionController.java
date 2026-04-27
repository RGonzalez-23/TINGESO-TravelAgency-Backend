package com.example.TINGESO.Controllers;

import com.example.TINGESO.Entities.PromotionEntity;
import com.example.TINGESO.Services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) return false;
        try {
            java.util.Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") != null) {
                java.util.List<String> roles = (java.util.List<String>) realmAccess.get("roles");
                return roles.contains("ADMIN");
            }
        } catch (Exception e) {}
        return false;
    }

    @GetMapping
    public ResponseEntity<?> getAllPromotions(@AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody PromotionEntity promotion, @AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.ok(promotionService.createPromotion(promotion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Long id, @RequestBody PromotionEntity promotion, @AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.ok(promotionService.updatePromotion(id, promotion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.ok("Promoción eliminada lógicamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
