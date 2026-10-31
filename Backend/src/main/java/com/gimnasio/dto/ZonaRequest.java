package com.gimnasio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ZonaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "Máximo 60 caracteres")
        String nombre,

        @NotBlank(message = "El tipo de zona es obligatorio")
        String tipo,

        @NotNull(message = "La capacidad máxima es obligatoria")
        @Min(value = 1, message = "La capacidad máxima debe ser mayor que cero")
        Integer capacidadMaxima,

        Boolean activa
) {
}
