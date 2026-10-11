package com.gimnasio.dto;

import com.gimnasio.domain.TurnoCola;

public record TurnoResponse(
        Long id,
        String tipo,
        String estado,
        int posicion,
        String fechaSolicitud,
        String horaLLamado,
        long personasDelante,
        int esperaEstimadaMin,
        boolean esMiTurno
) {
    public static TurnoResponse desde(TurnoCola turno, long personasDelante, int esperaEstimadaMin) {
        return new TurnoResponse(
                turno.getId(),
                turno.getTipo().getValor(),
                turno.getEstado().getValor(),
                turno.getPosicion(),
                turno.getFechaSolicitud().toString(),
                turno.getHoraLLamado() == null ? null : turno.getHoraLLamado().toString(),
                personasDelante,
                esperaEstimadaMin,
                turno.getEstado() == com.gimnasio.domain.enums.EstadoTurno.LLAMADO);
    }
}
