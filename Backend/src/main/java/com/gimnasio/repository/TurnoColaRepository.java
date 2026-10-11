package com.gimnasio.repository;

import com.gimnasio.domain.TurnoCola;
import com.gimnasio.domain.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TurnoColaRepository extends JpaRepository<TurnoCola, Long> {

    Optional<TurnoCola> findFirstByUsuarioIdAndEstadoIn(Long usuarioId, List<EstadoTurno> estados);

    /** Turnos en curso (esperando o llamados). */
    List<TurnoCola> findByEstadoInOrderByFechaSolicitudAsc(List<EstadoTurno> estados);

    long countByEstadoIn(List<EstadoTurno> estados);

    @Modifying
    @Query("UPDATE TurnoCola t SET t.estado = :nuevoEstado, t.horaLLamado = NULL " +
           "WHERE t.estado = :estadoActual AND t.horaLLamado < :limite")
    int expirarTurnos(@Param("estadoActual") EstadoTurno estadoActual,
                      @Param("nuevoEstado") EstadoTurno nuevoEstado,
                      @Param("limite") LocalDateTime limite);
}
