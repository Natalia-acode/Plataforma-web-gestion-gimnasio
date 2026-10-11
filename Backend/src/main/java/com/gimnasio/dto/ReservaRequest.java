package com.gimnasio.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservaRequest(
        @NotNull(message = "La clase es obligatoria")
        Long idClase,

        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "No se pueden reservar clases en fechas pasadas")
        LocalDate fecha
) {
}
