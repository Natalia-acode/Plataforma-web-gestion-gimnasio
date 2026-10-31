package com.gimnasio.dto;

import com.gimnasio.domain.Zona;
import com.gimnasio.domain.enums.TipoZona;

public record ZonaResponse(
        Long id,
        String nombre,
        TipoZona tipo,
        Integer capacidadMaxima,
        boolean activa
) {
    public static ZonaResponse desde(Zona zona) {
        return new ZonaResponse(
                zona.getId(),
                zona.getNombre(),
                zona.getTipo(),
                zona.getCapacidadMaxima(),
                zona.isActiva()
        );
    }
}
