package com.gimnasio.dto;

import com.gimnasio.domain.Clase;

public record ClaseResponse(
        Long id,
        String nombre,
        String diaSemana,
        String horaInicio,
        String horaFin,
        int cupoMaximo,
        boolean activa,
        Long entrenadorId,
        String entrenadorNombre,
        Long zonaId,
        String zonaNombre,
        Long reservasConfirmadas,
        Integer cuposDisponibles
) {
    public static ClaseResponse desde(Clase clase, long confirmadas) {
        return new ClaseResponse(
                clase.getId(),
                clase.getNombre(),
                clase.getDiaSemana().getValor(),
                clase.getHoraInicio().toString(),
                clase.getHoraFin().toString(),
                clase.getCupoMaximo(),
                clase.isActiva(),
                clase.getEntrenador().getId(),
                clase.getEntrenador().getNombreCompleto(),
                clase.getZona().getId(),
                clase.getZona().getNombre(),
                confirmadas,
                (int) Math.max(0, clase.getCupoMaximo() - confirmadas));
    }
}
