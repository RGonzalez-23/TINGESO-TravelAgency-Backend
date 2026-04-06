package com.example.TINGESO.Services;

import com.example.TINGESO.Entities.UserEntity;
import com.example.TINGESO.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public UserEntity login(String email, String password) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            // Comparación de contraseña en texto plano (temporal)
            if (user.getPassword().equals(password)) {
                System.out.println("-> ¡Las claves coinciden! Login Exitoso.");
                return user;
            } else {
                System.out.println("-> ERROR: Las claves no coinciden.");
            }
        } else {
            System.out.println("-> ERROR: El correo NO fue encontrado en la Base de Datos.");
        }
        return null; // Credenciales inválidas
    }

    public UserEntity registerClient(com.example.TINGESO.DTOs.RegisterRequest req) {
        // Verificaciones de duplicados (Para que no explote la BD)
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
        if (userRepository.existsByUserRut(req.getRut())) {
            throw new RuntimeException("El RUT ya está registrado");
        }

        UserEntity user = new UserEntity();
        // Concatenar el nombre completo sumando los campos del DTO
        String fullName = req.getNombres().trim() + " " + req.getApellidoPaterno().trim() + " " + req.getApellidoMaterno().trim();
        user.setFullName(fullName);
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword()); // Próximamente lo blindaremos
        user.setUserRut(req.getRut());
        user.setNationality(req.getNacionalidad());
        user.setPhone(req.getPhone());
        
        // Forzamos valores fijos para clientes nuevos
        user.setRole(com.example.TINGESO.Entities.RoleEnum.CLIENT);
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        
        return userRepository.save(user);
    }
}
