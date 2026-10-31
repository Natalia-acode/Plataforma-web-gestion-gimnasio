package com.gimnasio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "Máximo 80 caracteres")
        @Pattern(regexp = "^[\\p{L}\\p{M}ñÑáéíóúÁÉÍÓÚüÜ' .-]+$",
                message = "El nombre solo puede contener letras, espacios, acentos, comillas simples, puntos y guiones")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Máximo 80 caracteres")
        @Pattern(regexp = "^[\\p{L}\\p{M}ñÑáéíóúÁÉÍÓÚüÜ' .-]+$",
                message = "Los apellidos solo pueden contener letras, espacios, acentos, comillas simples, puntos y guiones")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo electrónico inválido")
        @Size(max = 120, message = "Máximo 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @Size(max = 20, message = "Máximo 20 caracteres")
        String telefono,

        String fechaNacimiento,

        // Rol solicitado en el registro público. Si se omite o viene vacío se crea un cliente.
        // Las cuentas de personal quedan pendientes de aprobación, salvo en arranque (sin administrador activo).
        @Size(max = 20, message = "Máximo 20 caracteres")
        String rol
) {
}
