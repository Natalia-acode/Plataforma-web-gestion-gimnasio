package com.gimnasio.dto;

import jakarta.validation.constraints.NotNull;

/**
 * RN-01: la renovación automática es opt-in.
 * Si renovacionAutomatica = true, idMetodo es obligatorio (tarjeta tokenizada).
 */
public record ContratarSuscripcionRequest(
        @NotNull(message = "El plan es obligatorio")
        Long idPlan,

        Long idMetodo,

        boolean renovacionAutomatica
) {
}
