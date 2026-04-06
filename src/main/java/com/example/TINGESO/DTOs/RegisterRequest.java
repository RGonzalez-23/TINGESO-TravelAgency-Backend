package com.example.TINGESO.DTOs;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private String password;
    private String phone;
    private String rut;
    private String nacionalidad;
}
