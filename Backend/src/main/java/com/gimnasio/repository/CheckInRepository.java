package com.gimnasio.repository;

import com.gimnasio.domain.CheckIn;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    Optional<CheckIn> findFirstByUsuarioIdAndFechaHoraSalidaIsNull(Long usuarioId);

    /** Antigüedad de clientes dentro del gimnasio (RN-11: cierre por inactividad). */
    @EntityGraph(attributePaths = {"usuario"})
    List<CheckIn> findByFechaHoraSalidaIsNullAndFechaHoraEntradaBefore(LocalDateTime limite);

    long countByFechaHoraEntradaBetween(LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = {"usuario"})
    List<CheckIn> findByFechaHoraEntradaBetween(LocalDateTime desde, LocalDateTime hasta);

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<CheckIn> findById(Long id);

    /** Personas actualmente dentro (equivale a la vista vw_ocupacion_actual). */
    long countByFechaHoraSalidaIsNull();
}
