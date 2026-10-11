package com.gimnasio.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pasarela de pago (Gateway). En producción se conectaría a un proveedor
 * real (p. ej. Wompi, PayU, Stripe). Aquí se implementa un simulador:
 *  - tokenizarTarjeta NO persiste el número completo (RNF-02 / PCI-DSS).
 *  - Las referencias que contienen "RECHAZA" simulan un cobro rechazado.
 */
@Component
public class PasarelaPagoClient {

    public record Token(String token, String ultimosCuatro) {
    }

    public record Transaccion(String idTransaccion, boolean aprobada, String mensaje) {
    }

    public Token tokenizarTarjeta(String numeroTarjeta) {
        String ultimosCuatro = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        return new Token("tok_" + UUID.randomUUID(), ultimosCuatro);
    }

    public Transaccion cobrar(String token, BigDecimal monto, String referencia) {
        String idTransaccion = "trx_" + UUID.randomUUID();
        boolean aprobada = referencia == null || !referencia.toUpperCase().contains("RECHAZA");
        return aprobada
                ? new Transaccion(idTransaccion, true, "Transacción aprobada")
                : new Transaccion(idTransaccion, false, "Tarjeta rechazada por el emisor");
    }

    public void reembolsar(String idTransaccion) {
        throw new UnsupportedOperationException(
                "Los reembolsos se gestionan manualmente desde el panel de la pasarela (CU-29)");
    }
}
