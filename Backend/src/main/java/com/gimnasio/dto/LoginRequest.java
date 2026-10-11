package com.gimnasio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo electrónico inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena
) {
}
