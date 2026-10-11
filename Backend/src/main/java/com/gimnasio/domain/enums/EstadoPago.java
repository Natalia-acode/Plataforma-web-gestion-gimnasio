package com.gimnasio.domain.enums;

public enum EstadoPago {
    PENDIENTE("pendiente"),
    APROBADO("aprobado"),
    RECHAZADO("rechazado"),
    REEMBOLSADO("reembolsado");

    private final String valor;

    EstadoPago(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoPago desdeValor(String valor) {
        for (EstadoPago estado : values()) {
            if (estado.valor.equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pago desconocido: " + valor);
    }
}
