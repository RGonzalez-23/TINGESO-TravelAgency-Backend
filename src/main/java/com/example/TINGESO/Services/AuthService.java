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
                return user;
            }
        }
        return null; // Credenciales inválidas
    }
}
