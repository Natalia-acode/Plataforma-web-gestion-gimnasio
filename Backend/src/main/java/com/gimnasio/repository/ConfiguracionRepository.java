package com.gimnasio.repository;

import com.gimnasio.domain.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {

    /** La tabla configuración es de fila unica (CU-17). */
    Optional<Configuracion> findFirstByOrderByIdAsc();
}
