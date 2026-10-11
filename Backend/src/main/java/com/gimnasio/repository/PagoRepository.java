package com.gimnasio.repository;

import com.gimnasio.domain.Pago;
import com.gimnasio.domain.enums.EstadoPago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findBySuscripcionIdOrderByFechaPagoDesc(Long suscripcionId);

    @EntityGraph(attributePaths = {"metodo", "suscripcion", "suscripcion.plan"})
    List<Pago> findBySuscripcionUsuarioIdOrderByFechaPagoDesc(Long usuarioId);

    List<Pago> findByEstadoAndFechaPagoBetween(EstadoPago estado, LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = {"metodo", "suscripcion", "suscripcion.plan"})
    List<Pago> findByFechaPagoBetweenOrderByFechaPagoDesc(LocalDateTime desde, LocalDateTime hasta);

    @Override
    @EntityGraph(attributePaths = {"metodo", "suscripcion", "suscripcion.plan"})
    Optional<Pago> findById(Long id);
}
