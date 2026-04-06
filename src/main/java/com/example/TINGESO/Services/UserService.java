package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.UserCreateDTO;
import com.example.TINGESO.Entities.RoleEnum;
import com.example.TINGESO.Entities.UserEntity;
import com.example.TINGESO.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity registerUser(UserCreateDTO userDTO) {
        if (userDTO.getFullName() == null || userDTO.getEmail() == null || userDTO.getPassword() == null || userDTO.getUserRut() == null) {
            throw new IllegalArgumentException("El nombre completo, correo electrónico, contraseña y RUT son obligatorios.");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(userDTO.getEmail()).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado en el sistema.");
        }

        // Validación de RUT único
        if (userRepository.existsByUserRut(userDTO.getUserRut())) {
            throw new IllegalArgumentException("El RUT ingresado ya se encuentra registrado.");
        }

        UserEntity newUser = new UserEntity();
        newUser.setFullName(userDTO.getFullName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setPassword(userDTO.getPassword());
        newUser.setPhone(userDTO.getPhone());
        newUser.setUserRut(userDTO.getUserRut()); // Actualizado
        newUser.setNationality(userDTO.getNationality());
        newUser.setRole(RoleEnum.CLIENT);
        newUser.setIsActive(true);
        newUser.setIsLocked(false);
        newUser.setFailedLoginAttempts(0);

        return userRepository.save(newUser);
    }

    public void deactivateUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        user.setIsActive(false);
        userRepository.save(user);
    }
}