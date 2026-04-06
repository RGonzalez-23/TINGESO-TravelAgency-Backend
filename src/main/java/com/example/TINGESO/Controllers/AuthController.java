package com.example.TINGESO.Controllers;

import com.example.TINGESO.DTOs.LoginRequest;
import com.example.TINGESO.Entities.UserEntity;
import com.example.TINGESO.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite peticiones desde el frontend (ej. localhost:5173)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        UserEntity user = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login exitoso");
            response.put("user", user);
            // Temporalmente devolvemos los datos del usuario. 
            // Después reemplazaremos esto para devolver un JWT token.
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}
