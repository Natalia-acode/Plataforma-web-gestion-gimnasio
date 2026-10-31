package com.gimnasio.dto;

import com.gimnasio.domain.Usuario;

/**
 * CU-01: la cuenta de cliente se activa al instante y entrega token; la de personal
 * puede quedar pendiente de aprobación, en cuyo caso no hay token hasta que un administrador la apruebe.
 */
public record RegistroResponse(
        boolean pendienteDeAprobacion,
        String token,
        UsuarioResponse usuario
) {

    public static RegistroResponse cuentaActiva(String token, Usuario usuario) {
        return new RegistroResponse(false, token, UsuarioResponse.desde(usuario));
    }

    public static RegistroResponse cuentaPendiente(Usuario usuario) {
        return new RegistroResponse(true, null, UsuarioResponse.desde(usuario));
    }
}
