package com.gimnasio.repository;

import com.gimnasio.domain.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaRepository extends JpaRepository<Zona, Long> {

    List<Zona> findByActivaTrue();
}
