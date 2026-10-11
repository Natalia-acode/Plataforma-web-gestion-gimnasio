package com.gimnasio.dto;

public record AforoConfigRequest(
        Integer capacidadMaxima,
        Integer umbralMedio,
        Integer umbralAlto,
        Integer ventanaTurnoMin,
        Integer tiempoRotacionMin,
        Integer reintentosCobro
) {
}
