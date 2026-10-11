package com.gimnasio.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReporteResponse(
        String desde,
        String hasta,
        long totalIngresos,
        BigDecimal ingresosTotales,
        long checkIns,
        long suscripcionesNuevas,
        long suscripcionesCanceladas,
        long reservasTomadas,
        long inasistencias,
        List<PagoResponse> pagosDelPeriodo
) {
}
