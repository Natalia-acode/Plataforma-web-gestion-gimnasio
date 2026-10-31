package com.gimnasio.domain.enums;

public enum EstadoSuscripcion {
    ACTIVA("activa"),
    CANCELADA("cancelada"),
    VENCIDA("vencida"),
    SUSPENDIDA("suspendida");

    private final String valor;

    EstadoSuscripcion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoSuscripcion desdeValor(String valor) {
        for (EstadoSuscripcion estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de suscripción desconocido: " + valor);
    }
}
