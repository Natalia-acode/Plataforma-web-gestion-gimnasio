package com.gimnasio.domain.enums;

public enum EstadoReserva {
    CONFIRMADA("confirmada"),
    EN_ESPERA("en_espera"),
    CANCELADA("cancelada"),
    ASISTIO("asistio"),
    NO_ASISTIO("no_asistio");

    private final String valor;

    EstadoReserva(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoReserva desdeValor(String valor) {
        for (EstadoReserva estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de reserva desconocido: " + valor);
    }
}
