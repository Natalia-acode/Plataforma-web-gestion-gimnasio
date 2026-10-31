package com.gimnasio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
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

        @Size(max = 20, message = "Máximo 20 caracteres")
        String telefono,

        String fechaNacimiento
) {
}
