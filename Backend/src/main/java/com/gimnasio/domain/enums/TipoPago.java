package com.gimnasio.domain.enums;

public enum TipoPago {
    ALTA("alta"),
    RENOVACION("renovacion"),
    AJUSTE_CAMBIO_PLAN("ajuste_cambio_plan");

    private final String valor;

    TipoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoPago desdeValor(String valor) {
        for (TipoPago tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de pago desconocido: " + valor);
    }
}
