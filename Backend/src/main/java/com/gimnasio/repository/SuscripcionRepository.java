package com.gimnasio.repository;

import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    /** RN-10: un cliente solo puede tener una suscripción activa. */
    @EntityGraph(attributePaths = {"plan", "usuario"})
    Optional<Suscripcion> findFirstByUsuarioIdAndEstado(Long usuarioId, EstadoSuscripcion estado);

    @EntityGraph(attributePaths = {"plan", "usuario"})
    List<Suscripcion> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);

    @Override
    @EntityGraph(attributePaths = {"plan", "usuario"})
    Optional<Suscripcion> findById(Long id);

    /** Renovaciones por vencer dentro de N días (CU-12). */
    List<Suscripcion> findByEstadoAndFechaFinBetween(EstadoSuscripcion estado, LocalDate desde, LocalDate hasta);

    List<Suscripcion> findByEstadoAndFechaFinBefore(EstadoSuscripcion estado, LocalDate fecha);
}
