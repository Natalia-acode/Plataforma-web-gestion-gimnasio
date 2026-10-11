package com.gimnasio.domain.enums;

public enum TipoZona {
    PESAS("pesas"),
    CARDIO("cardio"),
    SALA_GRUPAL("sala_grupal"),
    FUNCIONAL("funcional");

    private final String valor;

    TipoZona(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoZona desdeValor(String valor) {
        for (TipoZona tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de zona desconocido: " + valor);
    }
}
