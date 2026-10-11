package com.gimnasio.domain.enums;

public enum EstadoUsuario {
    ACTIVO("activo"),
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
            if (estado.valor.equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de usuario desconocido: " + valor);
    }
}
