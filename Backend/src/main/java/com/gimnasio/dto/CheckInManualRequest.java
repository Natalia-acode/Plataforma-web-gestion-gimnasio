package com.gimnasio.dto;

import jakarta.validation.constraints.NotNull;

/** Check-in manual desde recepcion (CU-13, alternativa al QR). */
public record CheckInManualRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario
) {
}
