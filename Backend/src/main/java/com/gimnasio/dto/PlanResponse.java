package com.gimnasio.dto;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer duracionDias,
        boolean activo
) {
}
