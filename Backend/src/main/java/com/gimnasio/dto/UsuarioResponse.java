package com.gimnasio.dto;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String apellidos,
        String nombreCompleto,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        Rol rol,
        String estado,
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getFechaNacimiento(),
                usuario.getRol(),
                usuario.getEstado().getValor(),
                usuario.getFechaRegistro());
    }
}
