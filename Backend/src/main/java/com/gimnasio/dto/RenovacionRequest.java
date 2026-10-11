package com.gimnasio.dto;

import jakarta.validation.constraints.NotNull;

public record RenovacionRequest(
        @NotNull(message = "Indique si desea activar o desactivar la renovación automática")
        Boolean activa
) {
}
