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

    // Inyectamos esto temporalmente para poder listar los usuarios fácilmente
    @Autowired
    private com.example.TINGESO.Repositories.UserRepository userRepository;

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

    // ⭐ ENDPOINT PARA REGISTRAR NUEVO CLIENTE ⭐
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody com.example.TINGESO.DTOs.RegisterRequest registerRequest) {
        try {
            UserEntity newUser = authService.registerClient(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (RuntimeException e) {
            // Devuelve error 400 Bad Request si el correo o rut ya existe
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ⭐ ENDPOINT TEMPORAL PARA VERIFICAR LA BASE DE DATOS ⭐
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
