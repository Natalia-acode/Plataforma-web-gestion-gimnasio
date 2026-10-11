package com.gimnasio.dto;

import jakarta.validation.constraints.Size;

public record CancelarSuscripcionRequest(
        @Size(max = 255, message = "Máximo 255 caracteres")
        String motivo
) {
}
