package com.gimnasio.dto;

import jakarta.validation.constraints.NotNull;

public record VentaPresencialRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long idUsuario,

        @NotNull(message = "El plan es obligatorio")
        Long idPlan,

        Long idMetodo,

        boolean renovacionAutomatica,

        boolean pagoEfectivo,

        String observacion
) {
}
