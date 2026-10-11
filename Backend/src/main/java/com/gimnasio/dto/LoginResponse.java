package com.gimnasio.dto;

import com.gimnasio.domain.Usuario;

public record LoginResponse(String token, UsuarioResponse usuario) {

    public static LoginResponse crear(String token, Usuario usuario) {
        return new LoginResponse(token, UsuarioResponse.desde(usuario));
    }
}
