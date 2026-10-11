package com.gimnasio.repository;

import com.gimnasio.domain.RegistroAforo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RegistroAforoRepository extends JpaRepository<RegistroAforo, Long> {

    List<RegistroAforo> findByFecha(LocalDate fecha);

    List<RegistroAforo> findByFechaBetweenOrderByFechaAscHoraAsc(LocalDate desde, LocalDate hasta);

    boolean existsByFechaAndHora(LocalDate fecha, byte hora);

    /** vw_afluencia_por_hora: promedios por día de la semana (0=lunes) y hora (CU-16). */
    @Query(value = "SELECT dia_semana, hora, ocupacion_promedio, ocupacion_maxima " +
                   "FROM vw_afluencia_por_hora WHERE hora BETWEEN :horaDesde AND :horaHasta " +
                   "ORDER BY dia_semana, hora", nativeQuery = true)
    List<AfluenciaHoraProjection> afluenciaPorHora(@Param("horaDesde") int horaDesde,
                                                   @Param("horaHasta") int horaHasta);

    interface AfluenciaHoraProjection {
        Integer getDia_semana();

        Integer getHora();

        BigDecimal getOcupacion_promedio();

        Integer getOcupacion_maxima();
    }
}
