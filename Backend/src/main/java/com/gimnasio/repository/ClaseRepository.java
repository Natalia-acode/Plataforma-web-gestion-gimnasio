package com.gimnasio.repository;

import com.gimnasio.domain.Clase;
import com.gimnasio.domain.enums.DiaSemana;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

    @EntityGraph(attributePaths = {"entrenador", "zona"})
    List<Clase> findByActivaTrue();

    @EntityGraph(attributePaths = {"entrenador", "zona"})
    List<Clase> findByActivaTrueAndDiaSemana(DiaSemana diaSemana);

    @Override
    @EntityGraph(attributePaths = {"entrenador", "zona"})
    Optional<Clase> findById(Long id);
}
