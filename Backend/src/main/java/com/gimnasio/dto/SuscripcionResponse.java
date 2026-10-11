package com.gimnasio.dto;

import com.gimnasio.domain.Suscripcion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SuscripcionResponse(
        Long id,
        Long planId,
        String planNombre,
        BigDecimal precio,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado,
        boolean renovacionAutomatica,
        Integer intentosCobro,
        LocalDateTime fechaCancelacion,
        String motivoCancelacion,
        Long usuarioId,
        String usuarioNombre
) {
    public static SuscripcionResponse desde(Suscripcion suscripcion) {
        return new SuscripcionResponse(
                suscripcion.getId(),
                suscripcion.getPlan().getId(),
                suscripcion.getPlan().getNombre(),
                suscripcion.getPlan().getPrecio(),
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin(),
                suscripcion.getEstado().getValor(),
                suscripcion.isRenovacionAutomatica(),
                suscripcion.getIntentosCobro(),
                suscripcion.getFechaCancelacion(),
                suscripcion.getMotivoCancelacion(),
                suscripcion.getUsuario().getId(),
                suscripcion.getUsuario().getNombreCompleto());
    }
}
