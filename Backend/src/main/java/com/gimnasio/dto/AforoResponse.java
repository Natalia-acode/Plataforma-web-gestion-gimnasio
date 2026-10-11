package com.gimnasio.dto;

/** Semáforo de ocupación en tiempo real (CU-15) y mapa de calor (CU-16). */
public record AforoResponse(
        int personasDentro,
        int capacidadMaxima,
        int porcentajeOcupacion,
        String nivel,          // BAJO | MEDIO | ALTO
        String recomendacion
) {
}
