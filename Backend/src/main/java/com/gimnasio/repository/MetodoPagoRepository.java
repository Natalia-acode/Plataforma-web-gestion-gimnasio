package com.gimnasio.repository;

import com.gimnasio.domain.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    List<MetodoPago> findByUsuarioIdAndActivoTrue(Long usuarioId);
}
