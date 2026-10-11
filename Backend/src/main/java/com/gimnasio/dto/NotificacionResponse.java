package com.gimnasio.dto;

import com.gimnasio.domain.Notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        String tipo,
        String mensaje,
        LocalDateTime fechaEnvio,
        boolean leida
) {
    public static NotificacionResponse desde(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTipo().getValor(),
                notificacion.getMensaje(),
                notificacion.getFechaEnvio(),
                notificacion.isLeida());
    }
}
