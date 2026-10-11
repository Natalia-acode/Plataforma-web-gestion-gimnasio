package com.gimnasio.domain.enums;

public enum TipoTurno {
    INGRESO("ingreso"),
    CLASE("clase");

    private final String valor;

    TipoTurno(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoTurno desdeValor(String valor) {
        for (TipoTurno tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de turno desconocido: " + valor);
    }
}
