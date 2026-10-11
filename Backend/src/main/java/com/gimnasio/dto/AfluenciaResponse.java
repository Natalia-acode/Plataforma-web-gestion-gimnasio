package com.gimnasio.dto;

import java.util.List;

public record AfluenciaResponse(List<CeldaAfluencia> celdas, CeldaAfluencia horaMasConcurrida,
                                CeldaAfluencia horaMenosConcurrida) {

    public record CeldaAfluencia(String diaSemana, int diaIndice, int hora, double ocupacionPromedio,
                                 int ocupacionMaxima) {
    }
}
