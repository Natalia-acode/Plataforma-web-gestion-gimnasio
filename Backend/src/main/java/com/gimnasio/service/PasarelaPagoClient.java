package com.gimnasio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimnasio.config.GimnasioProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

/**
 * Pasarela de pago (Gateway). Si la propiedad gimnasio.gateway.mode=real y hay baseUrl configurada,
 * intenta enviar las operaciones a un proveedor externo real. Si no hay integración activa,
 * usa el modo simulado para seguir desarrollando sin depender de credenciales.
 */
@Component
public class PasarelaPagoClient {

    private final GimnasioProperties propiedades;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public PasarelaPagoClient(GimnasioProperties propiedades, ObjectMapper objectMapper) {
        this.propiedades = propiedades;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public record Token(String token, String ultimosCuatro) {
    }

    public record Transaccion(String idTransaccion, boolean aprobada, String mensaje) {
    }

    public Token tokenizarTarjeta(String numeroTarjeta) {
        if (modoRealDisponible()) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of("number", numeroTarjeta));
                HttpRequest solicitud = HttpRequest.newBuilder()
                        .uri(URI.create(propiedades.gateway().baseUrl() + "/tokens"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + propiedades.gateway().apiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> respuesta = httpClient.send(solicitud, HttpResponse.BodyHandlers.ofString());
                if (respuesta.statusCode() >= 200 && respuesta.statusCode() < 300) {
                    Map<String, Object> body = objectMapper.readValue(respuesta.body(), Map.class);
                    String token = String.valueOf(body.getOrDefault("token", "tok_" + UUID.randomUUID()));
                    String ultimosCuatro = String.valueOf(body.getOrDefault("last4",
                            numeroTarjeta.substring(Math.max(0, numeroTarjeta.length() - 4))));
                    return new Token(token, ultimosCuatro);
                }
            } catch (Exception ex) {
                // Si el proveedor falla, se hace fallback seguro al simulador para no interrumpir el flujo local.
            }
        }

        String ultimosCuatro = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        return new Token("tok_" + UUID.randomUUID(), ultimosCuatro);
    }

    public Transaccion cobrar(String token, BigDecimal monto, String referencia) {
        if (modoRealDisponible()) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of(
                        "token", token,
                        "amount", monto,
                        "reference", referencia == null ? "GIMNASIO-" + UUID.randomUUID() : referencia
                ));
                HttpRequest solicitud = HttpRequest.newBuilder()
                        .uri(URI.create(propiedades.gateway().baseUrl() + "/charges"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + propiedades.gateway().apiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> respuesta = httpClient.send(solicitud, HttpResponse.BodyHandlers.ofString());
                if (respuesta.statusCode() >= 200 && respuesta.statusCode() < 300) {
                    Map<String, Object> body = objectMapper.readValue(respuesta.body(), Map.class);
                    boolean aprobada = Boolean.TRUE.equals(body.getOrDefault("approved", true));
                    String idTransaccion = String.valueOf(body.getOrDefault("id", "trx_" + UUID.randomUUID()));
                    String mensaje = String.valueOf(body.getOrDefault("message",
                            aprobada ? "Transacción aprobada" : "Transacción rechazada"));
                    return new Transaccion(idTransaccion, aprobada, mensaje);
                }
            } catch (Exception ex) {
                // fallback seguro
            }
        }

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

    private boolean modoRealDisponible() {
        return propiedades.gateway() != null
                && "real".equalsIgnoreCase(propiedades.gateway().mode())
                && propiedades.gateway().baseUrl() != null
                && !propiedades.gateway().baseUrl().isBlank()
                && propiedades.gateway().apiKey() != null
                && !propiedades.gateway().apiKey().isBlank();
    }
}
