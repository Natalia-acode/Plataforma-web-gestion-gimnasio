package com.gimnasio.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResultadoCobro(PagoResponse pago, BigDecimal monto, boolean aprobado, String mensaje) {

    public static ResultadoCobro aprobado(PagoResponse pago, BigDecimal monto) {
        return new ResultadoCobro(pago, monto, true, "Cobro aprobado");
    }

    public static ResultadoCobro rechazado(BigDecimal monto, String mensaje) {
        return new ResultadoCobro(null, monto, false, mensaje);
    }

    public record Lote(List<ResultadoCobro> resultados) {
    }
}
