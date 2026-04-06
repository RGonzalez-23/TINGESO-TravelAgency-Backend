package com.example.TINGESO.DTOs;

import lombok.Data;

@Data
public class UserCreateDTO {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String userRut; // Actualizado
    private String nationality;
}
