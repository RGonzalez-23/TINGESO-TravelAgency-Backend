package com.example.TINGESO.Controllers;

import com.example.TINGESO.DTOs.TourPackageRequest;
import com.example.TINGESO.Entities.TourPackageEntity;
import com.example.TINGESO.Services.TourPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*") // Habilitar peticiones desde React (Vite 5173)
public class TourPackageController {

    @Autowired
    private TourPackageService tourPackageService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(tourPackageService.getAllPackages());
    }

    // ⭐ ENDPOINT ÉPICA 3: Búsqueda y filtrado para clientes refactorizado
    @GetMapping("/search")
    public ResponseEntity<?> searchPackages(@ModelAttribute com.example.TINGESO.DTOs.TourPackageFilterDTO filterDTO) {
        try {
            return ResponseEntity.ok(tourPackageService.searchPackages(filterDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error en la búsqueda: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tourPackageService.getPackageById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TourPackageRequest request) {
        try {
            TourPackageEntity newPackage = tourPackageService.createPackage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPackage);
        } catch (RuntimeException e) {
            // Devuelve error 400 Bad Request cuando caen las reglas de negocio
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TourPackageRequest request) {
        try {
            TourPackageEntity updatedPackage = tourPackageService.updatePackage(id, request);
            return ResponseEntity.ok(updatedPackage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
