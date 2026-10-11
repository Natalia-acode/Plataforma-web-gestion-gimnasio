package com.gimnasio.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarPlanRequest(
        @NotNull(message = "El nuevo plan es obligatorio")
        Long idPlan
) {
}
