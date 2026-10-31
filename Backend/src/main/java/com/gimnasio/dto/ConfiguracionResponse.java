package com.gimnasio.dto;

/** Configuracion vigente del sistema (CU-17). */
public record ConfiguracionResponse(
        Long id,
        int capacidadMaxima,
        int umbralMedio,
        int umbralAlto,
        int ventanaTurnoMin,
        int tiempoRotacionMin,
        int reintentosCobro,
        String horarioApertura,
        String horarioCierre
) {
}
