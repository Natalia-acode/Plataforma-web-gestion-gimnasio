package com.gimnasio.repository;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findByRolAndEstado(Rol rol, EstadoUsuario estado);

    Optional<Usuario> findByPasswordResetToken(String passwordResetToken);

    List<Usuario> findByEstadoOrderByFechaRegistroAsc(EstadoUsuario estado);
}
