package com.gimnasio.domain.enums;

public enum Rol {
    CLIENTE("cliente"),
    RECEPCIONISTA("recepcionista"),
    ENTRENADOR("entrenador"),
    ADMINISTRADOR("administrador");

    private final String valor;

    Rol(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static Rol desdeValor(String valor) {
        for (Rol rol : values()) {
            if (rol.valor.equalsIgnoreCase(valor)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
