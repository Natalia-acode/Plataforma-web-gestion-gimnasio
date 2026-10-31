package com.gimnasio.domain.enums;

public enum EstadoTurno {
    ESPERANDO("esperando"),
    LLAMADO("llamado"),
    ATENDIDO("atendido"),
    CANCELADO("cancelado"),
    EXPIRADO("expirado");

    private final String valor;

    EstadoTurno(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoTurno desdeValor(String valor) {
        for (EstadoTurno estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de turno desconocido: " + valor);
    }
}
