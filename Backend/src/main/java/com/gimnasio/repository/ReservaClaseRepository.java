package com.gimnasio.repository;

import com.gimnasio.domain.ReservaClase;
import com.gimnasio.domain.enums.EstadoReserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaClaseRepository extends JpaRepository<ReservaClase, Long> {

    long countByClaseIdAndFechaAndEstado(Long claseId, LocalDate fecha, EstadoReserva estado);

    long countByClaseIdAndFechaAndEstadoIn(Long claseId, LocalDate fecha, List<EstadoReserva> estados);

    Optional<ReservaClase> findFirstByClaseIdAndUsuarioIdAndFechaAndEstadoIn(
            Long claseId, Long usuarioId, LocalDate fecha, List<EstadoReserva> estados);

    /** Lista de espera ordenada por antigüedad (RN-09: promoción automática). */
    List<ReservaClase> findByClaseIdAndFechaAndEstadoOrderByPosicionEsperaAsc(
            Long claseId, LocalDate fecha, EstadoReserva estado);

    @EntityGraph(attributePaths = {"clase"})
    List<ReservaClase> findByUsuarioIdAndEstadoInOrderByFechaDesc(Long usuarioId, List<EstadoReserva> estados);

    @EntityGraph(attributePaths = {"clase", "usuario"})
    List<ReservaClase> findByClaseIdAndFechaAndEstadoInOrderByFechaReservaAsc(
            Long claseId, LocalDate fecha, List<EstadoReserva> estados);

    @Override
    @EntityGraph(attributePaths = {"clase"})
    Optional<ReservaClase> findById(Long id);

    long countByFechaAndEstado(LocalDate fecha, EstadoReserva estado);
}
