package com.gimnasio.domain.enums;

public enum MetodoCheckIn {
    QR("qr"),
    MANUAL("manual");

    private final String valor;

    MetodoCheckIn(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static MetodoCheckIn desdeValor(String valor) {
        for (MetodoCheckIn metodo : values()) {
            if (metodo.valor.equalsIgnoreCase(valor)) {
                return metodo;
            }
        }
        throw new IllegalArgumentException("Método de check-in desconocido: " + valor);
    }
}
