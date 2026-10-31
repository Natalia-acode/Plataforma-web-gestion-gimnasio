package com.gimnasio.domain.enums;

public enum DiaSemana {
    LUNES("lunes", 0),
    MARTES("martes", 1),
    MIERCOLES("miercoles", 2),
    JUEVES("jueves", 3),
    VIERNES("viernes", 4),
    SABADO("sabado", 5),
    DOMINGO("domingo", 6);

    private final String valor;
    private final int indice;   // 0 = lunes ... 6 = domingo (vw_afluencia_por_hora)

    DiaSemana(String valor, int indice) {
        this.valor = valor;
        this.indice = indice;
    }

    public String getValor() {
        return valor;
    }

    public int getIndice() {
        return indice;
    }

    public static DiaSemana desdeValor(String valor) {
        for (DiaSemana dia : values()) {
            if (dia.valor.equalsIgnoreCase(valor)) {
                return dia;
            }
        }
        throw new IllegalArgumentException("Día de la semana desconocido: " + valor);
    }

    public static DiaSemana desdeIndice(int indice) {
        for (DiaSemana dia : values()) {
            if (dia.indice == indice) {
                return dia;
            }
        }
        throw new IllegalArgumentException("Índice de día desconocido: " + indice);
    }

    public static DiaSemana desde(java.time.DayOfWeek dayOfWeek) {
        return desdeIndice(dayOfWeek.getValue() - 1);
    }
}
