package com.gimnasio.domain.enums;

public enum EstadoUsuario {
    ACTIVO("activo"),
    PENDIENTE("pendiente"),
    RECHAZADO("rechazado"),
    SUSPENDIDO("suspendido");

    private final String valor;

    EstadoUsuario(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoUsuario desdeValor(String valor) {
        for (EstadoUsuario estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de usuario desconocido: " + valor);
    }
}
