package com.gimnasio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClaseRequest(
        @NotBlank(message = "El nombre de la clase es obligatorio")
        String nombre,

        @NotBlank(message = "Indique el día de la semana (lunes..domingo)")
        String diaSemana,

        @NotBlank(message = "La hora de inicio es obligatoria (HH:mm)")
        String horaInicio,

        @NotBlank(message = "La hora de fin es obligatoria (HH:mm)")
        String horaFin,

        @NotNull(message = "El cupo máximo es obligatorio")
        Integer cupoMaximo,

        @NotNull(message = "El entrenador es obligatorio")
        Long idEntrenador,

        @NotNull(message = "La zona es obligatoria")
        Long idZona
) {
}
