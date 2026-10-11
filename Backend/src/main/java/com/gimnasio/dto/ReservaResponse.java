package com.gimnasio.dto;

import com.gimnasio.domain.ReservaClase;

public record ReservaResponse(
        Long id,
        Long claseId,
        String claseNombre,
        String diaSemana,
        String horaInicio,
        String fecha,
        String estado,
        Integer posicionEspera
) {
    public static ReservaResponse desde(ReservaClase reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getClase().getId(),
                reserva.getClase().getNombre(),
                reserva.getClase().getDiaSemana().getValor(),
                reserva.getClase().getHoraInicio().toString(),
                reserva.getFecha().toString(),
                reserva.getEstado().getValor(),
                reserva.getPosicionEspera());
    }
}
