package com.gimnasio.dto;

import jakarta.validation.constraints.NotBlank;

public record TurnoRequest(
        @NotBlank(message = "Indique el tipo de turno (ingreso o clase)")
        String tipo
) {
}
