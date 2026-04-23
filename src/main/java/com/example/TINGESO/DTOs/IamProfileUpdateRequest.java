package com.example.TINGESO.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IamProfileUpdateRequest {

    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;

    @Email(message = "El formato de correo no es valido")
    private String email;

    @Pattern(
            regexp = "^$|^(?:\\+?56)?9\\d{8}$",
            message = "El telefono debe tener formato chileno (+569XXXXXXXX o 9XXXXXXXX)"
    )
    private String phone;

    @Pattern(regexp = "^$|^\\d{7,8}-[\\dkK]$", message = "El RUT debe tener formato 12345678-9")
    private String rut;

    private String nacionalidad;

    private Boolean active;
}
