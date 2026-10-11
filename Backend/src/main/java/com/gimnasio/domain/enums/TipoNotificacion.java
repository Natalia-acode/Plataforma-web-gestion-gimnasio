package com.gimnasio.domain.enums;

public enum TipoNotificacion {
    RECORDATORIO_COBRO("recordatorio_cobro"),
    RESULTADO_COBRO("resultado_cobro"),
    CONFIRMACION_RESERVA("confirmacion_reserva"),
    TURNO_PROXIMO("turno_proximo"),
    CANCELACION("cancelacion"),
    AFORO("aforo"),
    CUENTA("cuenta");

    private final String valor;

    TipoNotificacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoNotificacion desdeValor(String valor) {
        for (TipoNotificacion tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de notificación desconocido: " + valor);
    }
}
