package com.gimnasio.repository;

import com.gimnasio.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaEnvioDesc(Long usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaEnvioDesc(Long usuarioId);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
}
