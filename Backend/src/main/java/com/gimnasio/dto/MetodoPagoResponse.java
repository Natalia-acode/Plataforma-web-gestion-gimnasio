package com.gimnasio.dto;

import com.gimnasio.domain.MetodoPago;

public record MetodoPagoResponse(
        Long id,
        String tipo,
        String ultimosCuatro,
        String titular,
        boolean predeterminado,
        boolean activo
) {
    public static MetodoPagoResponse desde(MetodoPago metodo) {
        return new MetodoPagoResponse(
                metodo.getId(),
                metodo.getTipo().getValor(),
                metodo.getUltimosCuatro(),
                metodo.getTitular(),
                metodo.isPredeterminado(),
                metodo.isActivo());
    }
}
