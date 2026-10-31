package com.gimnasio.domain.enums;

public enum TipoMetodoPago {
    TARJETA_CREDITO("tarjeta_credito"),
    TARJETA_DEBITO("tarjeta_debito");

    private final String valor;

    TipoMetodoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoMetodoPago desdeValor(String valor) {
        for (TipoMetodoPago tipo : values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de método de pago desconocido: " + valor);
    }
}
