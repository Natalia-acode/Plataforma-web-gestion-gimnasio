package com.gimnasio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "Máximo 60 caracteres")
        String nombre,

        @Size(max = 255, message = "Máximo 255 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
        BigDecimal precio,

        @NotNull(message = "La duración en días es obligatoria")
        Integer duracionDias,

        Boolean activo
) {
}
