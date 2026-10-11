package com.gimnasio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * RNF-02 (PCI-DSS): los datos completos de la tarjeta se envían directamente
 * a la pasarela, que devuelve un token. En el sistema solo se persiste el token.
 */
public record MetodoPagoRequest(
        @NotBlank(message = "Indique el tipo de tarjeta (tarjeta_credito o tarjeta_debito)")
        String tipo,

        @NotBlank(message = "El número de tarjeta es obligatorio")
        @Pattern(regexp = "\\d{13,19}", message = "El número de tarjeta debe tener entre 13 y 19 dígitos")
        String numeroTarjeta,

        @NotBlank(message = "El titular es obligatorio")
        @Size(max = 120, message = "Máximo 120 caracteres")
        String titular,

        boolean predeterminado
) {
}
