package com.gimnasio.dto;

import java.util.List;

/** Estado de arranque: mientras falte el primer administrador, el registro de personal se autoactiva. */
public record ArranqueResponse(
        boolean faltaAdministrador,
        List<String> rolesDisponibles
) {
}
