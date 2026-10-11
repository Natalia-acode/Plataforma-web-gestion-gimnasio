package com.gimnasio.dto;

import com.gimnasio.domain.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        Long suscripcionId,
        String planNombre,
        BigDecimal monto,
        LocalDateTime fechaPago,
        String tipo,
        String estado,
        String numeroFactura,
        String idTransaccionPasarela,
        String metodoResumen,
        String mensajeError
) {
    public static PagoResponse desde(Pago pago, String mensajeError) {
        String metodo = pago.getMetodo() == null
                ? "efectivo"
                : pago.getMetodo().getTipo().getValor() + " •••• " + pago.getMetodo().getUltimosCuatro();
        return new PagoResponse(
                pago.getId(),
                pago.getSuscripcion().getId(),
                pago.getSuscripcion().getPlan().getNombre(),
                pago.getMonto(),
                pago.getFechaPago(),
                pago.getTipo().getValor(),
                pago.getEstado().getValor(),
                pago.getNumeroFactura(),
                pago.getIdTransaccionPasarela(),
                metodo,
                mensajeError);
    }
}
