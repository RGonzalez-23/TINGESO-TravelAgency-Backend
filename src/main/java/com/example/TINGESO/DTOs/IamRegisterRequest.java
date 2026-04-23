package com.example.TINGESO.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IamRegisterRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apellidoMaterno;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato de correo no es valido")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$",
            message = "La contrasena debe tener minimo 12 caracteres, mayuscula, minuscula, digito y caracter especial"
    )
    private String password;

    @Pattern(
            regexp = "^$|^(?:\\+?56)?9\\d{8}$",
            message = "El telefono debe tener formato chileno (+569XXXXXXXX o 9XXXXXXXX)"
    )
    private String phone;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El RUT debe tener formato 12345678-9")
    private String rut;

    private String nacionalidad;
}
